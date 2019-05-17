package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Docmosis;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.pdf.generator.HTMLToPDFConverter;

import java.util.Map;

@Service
public class DocumentGeneratorService {

    private final HTMLToPDFConverter converter;
    private final DocumentTemplates templates;
    private final ObjectMapper mapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public DocumentGeneratorService(HTMLToPDFConverter converter, DocumentTemplates templates, ObjectMapper mapper) {
        this.converter = converter;
        this.templates = templates;
        this.mapper = mapper;
    }

    @SafeVarargs
    public final byte[] generateSubmittedFormPDF(CaseDetails caseDetails, Map.Entry<String, ?>... extraContextEntries) {
        Map<String, Object> context = mapper.convertValue(caseDetails, new TypeReference<Map<String, Object>>() {});

        for (Map.Entry<String, ?> entry : extraContextEntries) {
            context.put(entry.getKey(), entry.getValue());
        }

        byte[] template = templates.getHtmlTemplate();

        return converter.convert(template, context);
    }

    @SafeVarargs
    public final byte[] generateStandardDefPDF(CaseDetails caseDetails, Map.Entry<String, ?>... extraContextEntries) {

        // extract the May of K, V pairs from the case details object
        Map<String, Object> context = mapper.convertValue(caseDetails, new TypeReference<Map<String, Object>>() {});

        for (Map.Entry<String, ?> entry : extraContextEntries) {
            context.put(entry.getKey(), entry.getValue());
        }

        // create request entity
        // set request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Docmosis requestBody = new Docmosis("FPL-template-docmosis.docx",
            "generatedStandardDirection.pdf", caseDetails.getData());
        HttpEntity<Docmosis> request = new HttpEntity<Docmosis>(requestBody, headers);
        System.out.println("docmosis data=" + requestBody.getData());
        System.out.println("docmosis template name=" + requestBody.getTemplateName());
        System.out.println("docmosis file name=" + requestBody.getOutputName());
        System.out.println("request=" + request.toString());
        String requestUrl = "http://docmosis:80/rs/render";
        // call docmosis
        ResponseEntity<byte[]> response = restTemplate.exchange(requestUrl, HttpMethod.POST, request, byte[].class);
        // check http code for 200
        if (response != null && HttpStatus.OK.equals(response.getStatusCode())) {
            System.out.println("*******This worked");
        }

        return response.getBody();
    }


}
