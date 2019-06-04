package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.model.StartInstanceResponse;
import uk.gov.hmcts.reform.fpl.model.Task;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TaskService {

    private final RestTemplate restTemplate = new RestTemplate();
    private String instanceId;

    public StartInstanceResponse startProcess(String caseId,
                                              List<String> taskTitle,
                                              String dueDate,
                                              String emailDate,
                                              String authorization,
                                              String userId) {
        final String uri = "http://camunda:8080/engine-rest/process-definition/key/standard_direction_task/start";

        Map<String, Object> variables = ImmutableMap.of(
            "variables", ImmutableMap.builder()
                .put("caseId", ImmutableMap.of(
                    "value", caseId,
                    "type", "String"
                ))
                .put("authorization", ImmutableMap.of(
                    "value", authorization,
                    "type", "String"
                ))
                .put("creatorId", ImmutableMap.of(
                    "value", userId,
                    "type", "String"
                ))
                .put("dueDate", ImmutableMap.of(
                    "value", dueDate,
                    "type", "String"
                ))
                .put("emailDate", ImmutableMap.of(
                    "value", emailDate,
                    "type", "String"
                ))
                .put("status", ImmutableMap.of(
                    "value", "Incomplete",
                    "type", "String"
                ))
                .put("tasks", ImmutableMap.of(
                    "value", taskTitle.toString(),
                    "type", "Object",
                    "valueInfo", ImmutableMap.of(
                        "objectTypeName", "java.util.ArrayList",
                        "serializationDataFormat", "application/json"
                    )
                ))
                .build(),
            "withVariablesInReturn", "true"
        );

        System.out.println("variables = " + variables);

        StartInstanceResponse response = restTemplate.postForObject(uri, variables, StartInstanceResponse.class);

        instanceId = Objects.requireNonNull(response).getId();

        return response;
    }

    public Task getLocalVariables() {
        System.out.println("instanceId in get = " + instanceId);

        final String uri = "http://camunda:8080/engine-rest/execution/" + instanceId + "/localVariables";

        return restTemplate.getForObject(uri, Task.class);
    }
}
