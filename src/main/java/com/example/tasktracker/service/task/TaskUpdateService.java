package com.example.tasktracker.service.task;

import com.example.tasktracker.dto.task.request.TaskUpdateRequest;
import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.enums.*;
import com.example.tasktracker.service.task_history.TaskHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskUpdateService {

    private final TaskService taskService;
    private final TaskHistoryService taskHistoryService;

    @Transactional
    public TaskEntity updateTask(Long id, TaskUpdateRequest request) {

        TaskEntity taskEntity = taskService.getTaskById(id);

        String oldTitle = taskEntity.getTitle();
        String oldDescription = taskEntity.getDescription();
        Priority oldPriority = taskEntity.getPriority();
        Category oldCategory = taskEntity.getCategory();
        LocalDate oldDeadline = taskEntity.getDeadline();

        taskEntity.setTitle(request.getTitle());
        taskEntity.setDescription(request.getDescription());
        taskEntity.setPriority(request.getPriority());
        taskEntity.setDeadline(request.getDeadline());
        taskEntity.setCategory(request.getCategory());

        if (!Objects.equals(oldTitle, taskEntity.getTitle())) {
            taskHistoryService.addHistory(
                    taskEntity.getId(),
                    TaskHistoryAction.TITLE_CHANGED,
                    "Title changed",
                    oldTitle,
                    taskEntity.getTitle()
            );
        }

        if (!Objects.equals(oldDescription, taskEntity.getDescription())) {
            taskHistoryService.addHistory(
                    taskEntity.getId(),
                    TaskHistoryAction.DESCRIPTION_CHANGED,
                    "Description changed",
                    oldDescription,
                    taskEntity.getDescription()
            );
        }

        if (oldPriority != taskEntity.getPriority()) {
            taskHistoryService.addHistory(
                    taskEntity.getId(),
                    TaskHistoryAction.PRIORITY_CHANGED,
                    "Priority changed",
                    (oldPriority != null) ? oldPriority.name() : null,
                    taskEntity.getPriority() != null
                            ? taskEntity.getPriority().name()
                            : null
            );
        }

        if (oldCategory != taskEntity.getCategory()) {
            taskHistoryService.addHistory(
                    taskEntity.getId(),
                    TaskHistoryAction.CATEGORY_CHANGED,
                    "Category changed",
                    (oldCategory != null) ? oldCategory.name() : null,
                    taskEntity.getCategory() != null
                            ? taskEntity.getCategory().name()
                            : null
            );
        }

        if (!Objects.equals(oldDeadline, taskEntity.getDeadline())) {
            taskHistoryService.addHistory(
                    taskEntity.getId(),
                    TaskHistoryAction.DEADLINE_CHANGED,
                    "Deadline changed",
                    (oldDeadline != null) ? oldDeadline.toString() : null,
                    taskEntity.getDeadline() != null
                            ? taskEntity.getDeadline().toString()
                            : null
            );
        }

        return taskEntity;
    }
}
