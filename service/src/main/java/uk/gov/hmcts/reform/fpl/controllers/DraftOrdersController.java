package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.DirectionHelperService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

@Api
@RestController
@RequestMapping("/callback/draft-SDO")
public class DraftOrdersController {
    private final ObjectMapper mapper;
    private final DocmosisDocumentGeneratorService documentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final DirectionHelperService directionHelperService;

    @Autowired
    public DraftOrdersController(ObjectMapper mapper,
                                 DocmosisDocumentGeneratorService documentGeneratorService,
                                 UploadDocumentService uploadDocumentService,
                                 CaseDataExtractionService caseDataExtractionService,
                                 DirectionHelperService directionHelperService) {
        this.mapper = mapper;
        this.documentGeneratorService = documentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.caseDataExtractionService = caseDataExtractionService;
        this.directionHelperService = directionHelperService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (!isNull(caseData.getStandardDirectionOrder())) {
            Map<String, List<Element<Direction>>> directions = directionHelperService.sortDirectionsByAssignee(
                caseData.getStandardDirectionOrder().getDirections());

            directions.forEach((key, value) -> caseDetails.getData().put(key, value));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackrequest) throws IOException {
        CaseDetails caseDetailsBefore = addDirectionsToOrder(callbackrequest.getCaseDetailsBefore());
        CaseData caseDataBefore = mapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

        CaseDetails caseDetailsAfter = addDirectionsToOrder(callbackrequest.getCaseDetails());
        CaseData caseDataWithValuesRemoved = mapper.convertValue(caseDetailsAfter.getData(), CaseData.class);

        Order orderWithValues = directionHelperService.persistHiddenDirectionValues(
            caseDataBefore.getStandardDirectionOrder(), caseDataWithValuesRemoved.getStandardDirectionOrder());

        CaseData.CaseDataBuilder caseDataBuilder = caseDataWithValuesRemoved.toBuilder();

        caseDataBuilder.standardDirectionOrder(orderWithValues);

        Map<String, Object> templateData = caseDataExtractionService
            .getDraftStandardOrderDirectionTemplateData(caseDataBuilder.build());

        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, DocmosisTemplates.SDO);

        Document document = uploadDocumentService.uploadPDF(userId, authorization, docmosisDocument.getBytes(),
            "Draft.pdf");

        caseDetailsAfter.getData().put("sdo", DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename("Draft.pdf")
            .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsAfter.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetailsBefore = addDirectionsToOrder(callbackrequest.getCaseDetailsBefore());
        CaseData caseDataBefore = mapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

        CaseDetails caseDetailsAfter = addDirectionsToOrder(callbackrequest.getCaseDetails());
        CaseData caseDataWithValuesRemoved = mapper.convertValue(caseDetailsAfter.getData(), CaseData.class);

        Order orderWithValues = directionHelperService.persistHiddenDirectionValues(
            caseDataBefore.getStandardDirectionOrder(), caseDataWithValuesRemoved.getStandardDirectionOrder());

        caseDetailsAfter.getData().put("standardDirectionOrder", orderWithValues);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsAfter.getData())
            .build();
    }

    private CaseDetails addDirectionsToOrder(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseDetails.getData().put("standardDirectionOrder", directionHelperService.createOrder(caseData));

        return caseDetails;
    }
}
