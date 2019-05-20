package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.SubmittedFormFilenameHelper.buildFileName;

@SuppressWarnings("LineLength")
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

    @SuppressWarnings("unchecked")
    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestBody CallbackRequest callbackrequest) {

        System.out.println("START: ************ about to start handler *****************");

        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        Map<String, Object> data = caseDetails.getData();

        System.out.println("***case name=" + caseDetails.getData().get("caseName"));

        // create standard directions
        List<String> standardDirections = buildStandardDirections(caseDetails.getData());

        data.put("standardDirections", ImmutableList.builder()
            .add(ImmutableMap.builder()
                .put("id", "1")
                .put("value", ImmutableMap.builder()
                    .put("content", standardDirections.get(0))
                    .build())
                .build())
            .add(ImmutableMap.builder()
                .put("id", "2")
                .put("value", ImmutableMap.builder()
                    .put("content", standardDirections.get(1))
                    .build())
                .build())
            .build());

        data.put("caseName", caseDetails.getData().get("caseName"));

        System.out.println("END: ************ about to start handler *****************");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestHeader(value = "authorization")
                                                                   String authorization,
                                                               @RequestHeader(value = "user-id") String userId,
                                                               @RequestBody CallbackRequest callbackrequest) {

        System.out.println("START: ********* mid event handler ***************");
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        byte[] pdf = documentGeneratorService.generateStandardDefPDF(caseDetails,
            Pair.of("userFullName", userDetailsService.getUserName(authorization))
        );

        String fileName = buildFileName(caseDetails);
        System.out.println("filename=" + fileName);

        Document document = uploadDocumentService.uploadPDF(userId, authorization, pdf, fileName);

        Map<String, Object> data = caseDetails.getData();
        data.put("standardDirectionDocument", ImmutableMap.<String, String>builder()
            .put("document_url", document.links.self.href)
            .put("document_binary_url", document.links.binary.href)
            .put("document_filename", "generatedStandardDirectionPDF")
            .build());

        System.out.println("END: ************ mid event handler *****************");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private List<String> buildStandardDirections(Map<String, Object> data) {
        String judgeType = "Local";//(String) data.get("judgeType");
        String judgeName = "John the Judge";//(String) data.get("judgeName");
        String personName = "Joe Person Bloggs";//(String) data.get("personName");

        // TODO - MAKE DYNAMIC
        int numOfStandardDirections = 2;

        String children = "child";

        if (numOfStandardDirections > 1) {
            children = children + "ren";
        }

        // get standard directions from case data
        String sd1 = "2. The proceedings are allocated for case management to the "
            + judgeType + " reserved to " + judgeName + ".";
        String sd2 = "3. A children’s guardian must be appointed for the "
            + children + " preferably " + personName + ".";

        List<String> standardDirections = new ArrayList<String>();
        standardDirections.add(sd1);
        standardDirections.add(sd2);
        return standardDirections;
    }
}
