package com.example.tasktracker.service;

import com.example.tasktracker.dto.TaskHistoryResponse;
import com.example.tasktracker.entities.TaskHistoryEntity;
import com.example.tasktracker.enums.TaskHistoryAction;
import com.example.tasktracker.repository.TaskHistoryRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskHistoryService {

    private final TaskHistoryRepository taskHistoryRepository;

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

        return taskHistoryRepository
                .findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
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
