package com.example.tasktracker.service.task_history;

import com.example.tasktracker.dto.task_history.TaskHistoryResponse;
import com.example.tasktracker.entities.task_history.TaskHistoryEntity;
import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.enums.TaskHistoryAction;
import com.example.tasktracker.exception.custom_exceptions.TaskNotFoundException;
import com.example.tasktracker.repository.task.TaskRepository;
import com.example.tasktracker.repository.task_history.TaskHistoryRepository;
import com.example.tasktracker.service.user.CurrentUserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskHistoryService {

    private final TaskHistoryRepository taskHistoryRepository;
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public void addHistory(
            Long taskId,
            TaskHistoryAction action,
            String description,
            String oldValue,
            String newValue
    ) {
        TaskHistoryEntity historyEntity = TaskHistoryEntity.builder()
                .taskId(taskId)
                .action(action)
                .description(description)
                .oldValue(oldValue)
                .newValue(newValue)
                .createdAt(LocalDateTime.now())
                .build();

        taskHistoryRepository.save(historyEntity);
    }

    @Transactional(readOnly = true)
    public List<TaskHistoryResponse> getHistory(Long taskId) {

        checkTaskAccess(taskId);

        return taskHistoryRepository
                .findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void checkTaskAccess(Long taskId) {
        if (currentUserService.isAdmin()) {
            if (!taskRepository.existsById(taskId)) {
                throw new TaskNotFoundException();
            }

            return;
        }

        UserEntity owner = currentUserService.getCurrentUser();

        taskRepository.findByIdAndOwner(taskId, owner)
                .orElseThrow(TaskNotFoundException::new);
    }


    private TaskHistoryResponse toResponse(TaskHistoryEntity history) {

        return new TaskHistoryResponse(
                history.getId(),
                history.getTaskId(),
                history.getAction(),
                history.getDescription(),
                history.getOldValue(),
                history.getNewValue(),
                history.getCreatedAt()
        );
    }
}
