package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.SubmittedFormFilenameHelper.buildFileName;

@Api
@RestController
@RequestMapping("/callback/standard-direction")
public class StandardDirectionController {

    private final UserDetailsService userDetailsService;
    private final DocumentGeneratorService documentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public StandardDirectionController(
        UserDetailsService userDetailsService,
        DocumentGeneratorService documentGeneratorService,
        UploadDocumentService uploadDocumentService,
        ApplicationEventPublisher applicationEventPublisher) {
        this.userDetailsService = userDetailsService;
        this.documentGeneratorService = documentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackrequest) {
        String judgeType = "local ";
        String judgeName = "Joseph";
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        Map<String, Object> data = caseDetails.getData();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestHeader(value = "authorization")
                                                                       String authorization,
                                                               @RequestHeader(value = "user-id") String userId,
                                                               @RequestBody CallbackRequest callbackrequest) {

        System.out.println("*****HHHHHHHHHHHHHHHHHHHHHHHHHH*");
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        byte[] pdf = documentGeneratorService.generateStandardDefPDF(caseDetails,
            Pair.of("userFullName", userDetailsService.getUserName(authorization))
        );

        Document document = uploadDocumentService.uploadPDF(userId, authorization, pdf, buildFileName(caseDetails));

        Map<String, Object> data = caseDetails.getData();
        data.put("standardDirectionDocument", ImmutableMap.<String, String>builder()
            .put("document_url", document.links.self.href)
            .put("document_binary_url", document.links.binary.href)
            .put("document_filename", document.originalDocumentName)
            .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
