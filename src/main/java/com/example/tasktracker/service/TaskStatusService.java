package com.example.tasktracker.service;

import com.example.tasktracker.entities.TaskEntity;
import com.example.tasktracker.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class TaskStatusService {

    private final TaskService taskService;
    private final TaskHistoryService taskHistoryService;

    @Transactional
    public TaskEntity changeStatus(Long id, TaskStatus status) {

        TaskEntity taskEntity = taskService.getTaskById(id);

        TaskStatus oldStatus = taskEntity.getStatus();

        if (oldStatus == status) {
            return taskEntity;
        }

        taskEntity.setStatus(status);

        taskHistoryService.addHistory(
                taskEntity.getId(),
                TaskHistoryAction.STATUS_CHANGED,
                "Status changed",
                oldStatus.name(),
                status.name()
        );

        return taskEntity;
    }
}
