package uk.gov.hmcts.reform.fpl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class EmailNotificationService {

    private final NotificationClient notificationClient;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public EmailNotificationService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    public void sendNotification(String templateId, String email, Map<String, Object> parameters, String reference) {
        logger.debug("Sending submission notification (with template id: {}) to {}", templateId, email);
        try {
            notificationClient.sendEmail(templateId, email, parameters, reference);
        } catch (NotificationClientException e) {
            logger.error("Failed to send submission notification (with template id: {}) to {}", templateId, email, e);
        }
    }

    public void sendNotification(String templateId, List<String> emails, Map<String, Object> parameters, String reference) {
        logger.debug("Sending submission notification (with template id: {}) to {}", templateId, emails);

        if (!CollectionUtils.isEmpty(emails)) {
            emails.stream().filter(Objects::nonNull).forEach(email ->
                sendNotification(templateId, email, parameters, reference));
        }
    }
}

