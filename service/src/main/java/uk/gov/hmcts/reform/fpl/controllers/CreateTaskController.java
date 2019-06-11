package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.CCDTask;
import uk.gov.hmcts.reform.fpl.model.StartInstanceResponse;
import uk.gov.hmcts.reform.fpl.model.Task;
import uk.gov.hmcts.reform.fpl.service.TaskService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/create-task")
public class CreateTaskController {

    private final TaskService taskService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public CreateTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        List<Map<String, CCDTask>> taskObjects =
            (List<Map<String, CCDTask>>) callbackRequest.getCaseDetails().getData().get("task");

        List<CCDTask> tasks = getTaskList(taskObjects);

        DateTime dueDate = new DateTime().plusDays(7);
        DateTime emailDate = dueDate.minusDays(3);

        if (tasks.stream().anyMatch(task -> task.getStatus() == null)) {

            List<String> titles = new ArrayList<>();
            tasks.forEach(title -> titles.add("\"" + title.getTaskTitle() + "\""));

            StartInstanceResponse startInstanceResponse = taskService.startProcess(
                callbackRequest.getCaseDetails().getId().toString(),
                titles,
                dueDate.toString(),
                emailDate.toString(),
                authorization,
                userId
            );

            List<Map<String, CCDTask>> newTasks = new ArrayList<>();

            titles.forEach(title ->
                newTasks.add(ImmutableMap.of(
                    "value", CCDTask.builder()
                        .assignee("")
                        .taskTitle(title.replace("\"", ""))
                        .dueDate(startInstanceResponse.getVariables().getDueDate().getValue())
                        .emailDate(startInstanceResponse.getVariables().getEmailDate().getValue())
                        .status(startInstanceResponse.getVariables().getStatus().getValue())
                        .build()))
            );

            data.put("task", newTasks);
            data.put("orderCompletion", "Incomplete");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(@RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        List<Map<String, CCDTask>> taskObjects =
            (List<Map<String, CCDTask>>) callbackRequest.getCaseDetails().getData().get("task");

        if (taskObjects != null) {
            List<CCDTask> tasks = getTaskList(taskObjects);

            try {
                Task variables = taskService.getLocalVariables();

                List<Map<String, CCDTask>> newTasks = new ArrayList<>();

                tasks.forEach(task ->
                    newTasks.add(ImmutableMap.of(
                        "value", CCDTask.builder()
                            .assignee(task.getAssignee())
                            .taskTitle(task.getTaskTitle())
                            .dueDate(variables.getDueDate().getValue())
                            .emailDate(variables.getEmailDate().getValue())
                            .status(variables.getStatus().getValue())
                            .build()))
                );

                data.put("task", newTasks);

            } catch (Exception e) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(data)
                    .build();
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private List<CCDTask> getTaskList(List<Map<String, CCDTask>> taskObjects) {
        List<CCDTask> tasks = new ArrayList<>();
        taskObjects.forEach(object -> tasks.add(mapper.convertValue(object.get("value"), CCDTask.class)));

        return tasks;
    }
}
