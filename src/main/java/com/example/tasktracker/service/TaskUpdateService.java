package com.example.tasktracker.service;

import com.example.tasktracker.dto.TaskUpdateRequest;
import com.example.tasktracker.entities.TaskEntity;
import com.example.tasktracker.enums.*;
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

        boolean titleChanged =
                !Objects.equals(
                        taskEntity.getTitle(),
                        request.getTitle());

        boolean descriptionChanged =
                !Objects.equals(
                        taskEntity.getDescription(),
                        request.getDescription());

        Priority oldPriority = taskEntity.getPriority();
        Category oldCategory = taskEntity.getCategory();
        LocalDate oldDeadline = taskEntity.getDeadline();

        taskEntity.setTitle(request.getTitle());
        taskEntity.setDescription(request.getDescription());
        taskEntity.setPriority(request.getPriority());
        taskEntity.setDeadline(request.getDeadline());
        taskEntity.setCategory(request.getCategory());

        if (titleChanged || descriptionChanged) {
            taskHistoryService.addHistory(
                    taskEntity.getId(),
                    TaskHistoryAction.UPDATED,
                    "Task updated",
                    null,
                    null
            );
        }

        if (oldPriority != taskEntity.getPriority()) {
            taskHistoryService.addHistory(
                    taskEntity.getId(),
                    TaskHistoryAction.PRIORITY_CHANGED,
                    "Priority changed",
                    oldPriority != null ? oldPriority.name() : null,
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
                    oldCategory != null ? oldCategory.name() : null,
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
                    String.valueOf(oldDeadline),
                    String.valueOf(taskEntity.getDeadline())
            );
        }

        return taskEntity;
    }
}
