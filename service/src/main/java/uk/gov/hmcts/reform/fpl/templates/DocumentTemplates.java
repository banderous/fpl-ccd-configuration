package uk.gov.hmcts.reform.fpl.templates;

import org.springframework.stereotype.Component;

import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@SuppressWarnings("LineLength")
@Component
public class DocumentTemplates {

    public byte[] getHtmlTemplate() {
        return readBytes("HTMLTemplate.html");
    }

    public byte[] getSDTemplate() {
        return readBytes("/Users/rebeccabrennan/Documents/FamilyPublicLaw/fpl-ccd-configuration/docker/docmosis/templates/FPL-template-docmosis.docx");
    }

}
