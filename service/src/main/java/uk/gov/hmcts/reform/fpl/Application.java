package uk.gov.hmcts.reform.fpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.model.CCDTask;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAMUNDA_POC_TEMPLATE;

@SpringBootApplication
@EnableAsync
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    private static String NOTIFY_API_KEY;
    private static CoreCaseDataApi caseDataApi;
    private static AuthTokenGenerator authTokenGenerator;

    @Value("${NOTIFY_API_KEY}")
    public void setNotifyApiKey(String notifyApiKey) {
        NOTIFY_API_KEY = notifyApiKey;
    }

    @SuppressWarnings("unchecked")
    public static void main(final String[] args) {
        ConfigurableApplicationContext appContext = SpringApplication.run(Application.class, args);

        // bootstrap the client
        ExternalTaskClient client = ExternalTaskClient.create()
            .baseUrl("http://camunda:8080/engine-rest")
            .build();

        // subscribe to the topic
        client.subscribe("taskAssigned")
            .lockDuration(1000)
            .handler((externalTask, externalTaskService) -> {
                System.out.println("externalTask = " + externalTask.getAllVariables());

                String email = externalTask.getAllVariables().get("assignee").toString();
                String taskTitle = externalTask.getAllVariables().get("task").toString();

                System.out.println("****************************** taskTitle = " + taskTitle);

                NotificationClient notificationClient = new NotificationClient(NOTIFY_API_KEY);

                Map<String, Object> parameters = ImmutableMap.of("task", taskTitle);

                System.out.println("Sending submission notification to: " + email);
                try {
                    notificationClient.sendEmail(CAMUNDA_POC_TEMPLATE, email, parameters, "reference");
                } catch (NotificationClientException e) {
                    System.out.println("Failed to send submission notification to: " + email);
                }

                // complete the external task
                externalTaskService.complete(externalTask);

                System.out.println("The External Task " + externalTask.getId() + " has been completed!");

            }).open();

        client.subscribe("taskCompleted")
            .lockDuration(1000)
            .handler((externalTask, externalTaskService) -> {
                System.out.println("externalTask = " + externalTask.getAllVariables());

                String authorization = externalTask.getAllVariables().get("authorization").toString();
                String userId = externalTask.getAllVariables().get("creatorId").toString();
                String caseId = externalTask.getAllVariables().get("caseId").toString();

                CoreCaseDataApi caseDataApi = appContext.getBean(CoreCaseDataApi.class);
                AuthTokenGenerator authTokenGenerator = appContext.getBean(AuthTokenGenerator.class);

                System.out.println("authTokenGenerator = " + authTokenGenerator.generate());

                // start event
                StartEventResponse startEventResponse = caseDataApi.startEventForCaseWorker(
                    authorization, authTokenGenerator.generate(), userId, "PUBLICLAW",
                    "CARE_SUPERVISION_EPO", caseId, "taskUpdate");

                // set task object from case details
                List<Map<String, Object>> taskObjects =
                    (List<Map<String, Object>>) startEventResponse.getCaseDetails().getData().get("task");

                ObjectMapper mapper = new ObjectMapper();

                Map<String, CCDTask> tasksMap = new HashMap<>();
                taskObjects.forEach(object -> tasksMap.put(
                    object.get("id").toString(),
                    mapper.convertValue(object.get("value"), CCDTask.class))
                );

                Map<String, CCDTask> completedTask = tasksMap.entrySet().stream()
                    .filter(x ->
                        x.getValue().getTaskTitle().contains(externalTask.getAllVariables().get("task").toString()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

                Map<String, Object> taskToBeRemoved = ImmutableMap.of(
                    "id", completedTask.keySet().iterator().next(),
                    "value", ImmutableMap.of(
                        "status", completedTask.entrySet().iterator().next().getValue().getStatus(),
                        "dueDate", completedTask.entrySet().iterator().next().getValue().getDueDate(),
                        "assignee", "",
                        "emailDate", completedTask.entrySet().iterator().next().getValue().getEmailDate(),
                        "taskTitle", completedTask.entrySet().iterator().next().getValue().getTaskTitle()
                    ));

                taskObjects.remove(taskToBeRemoved);

                completedTask.replace(completedTask.keySet().iterator().next(), CCDTask.builder()
                    .assignee(completedTask.entrySet().iterator().next().getValue().getAssignee())
                    .taskTitle(completedTask.entrySet().iterator().next().getValue().getTaskTitle())
                    .dueDate(completedTask.entrySet().iterator().next().getValue().getDueDate())
                    .emailDate(completedTask.entrySet().iterator().next().getValue().getEmailDate())
                    .status("Complete")
                    .build());

                taskObjects.add(ImmutableMap.of(
                    "id", completedTask.keySet().iterator().next(),
                    "value", completedTask.entrySet().iterator().next().getValue()));

                // STANDARD DIRECTION STATUS

                int instancesBefore =
                    Integer.parseInt(externalTask.getAllVariables().get("nrOfActiveInstances").toString());

                if (instancesBefore == 1) {
                    System.out.println("STANDARD DIRECTIONS COMPLETE");
                }

                ////

                CaseDataContent caseData = CaseDataContent.builder()
                    .eventToken(startEventResponse.getToken())
                    .event(Event.builder()
                        .id(startEventResponse.getEventId())
                        .build())
                    .data(ImmutableMap.of("task", taskObjects))
                    .build();

                caseDataApi.submitEventForCaseWorker(
                    authorization, authTokenGenerator.generate(), userId, "PUBLICLAW",
                    "CARE_SUPERVISION_EPO", caseId, true, caseData);

                // complete the external task
                externalTaskService.complete(externalTask);

                System.out.println("The External Task " + externalTask.getId() + " has been completed!");

            }).open();

        client.subscribe("intervention")
            .lockDuration(1000)
            .handler((externalTask, externalTaskService) -> {
                System.out.println("externalTask = " + externalTask.getAllVariables());

                System.out.println("STANDARD DIRECTIONS ACCEPTED AS IS");

                externalTaskService.complete(externalTask);

                System.out.println("The External Task " + externalTask.getId() + " has been completed!");

            }).open();
    }
}
