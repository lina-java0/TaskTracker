package com.example.tasktracker.service;

import com.example.tasktracker.dto.*;
import com.example.tasktracker.enums.*;
import com.example.tasktracker.exception.TaskNotFoundException;
import com.example.tasktracker.entities.TaskEntity;
import com.example.tasktracker.repository.TaskRepository;
import com.example.tasktracker.specification.TaskSpecificationBuilder;
import com.example.tasktracker.validation.TaskFilterValidator;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskSortService taskSortService;
    private final TaskFilterValidator taskFilterValidator;
    private final TaskHistoryService taskHistoryService;

    public Page<TaskEntity> getTasks(
            TaskFilterRequest filterRequest,
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        taskFilterValidator.validate(filterRequest);

        Specification<TaskEntity> specification = TaskSpecificationBuilder.build(filterRequest);

        Sort sort = taskSortService.getSort(filterRequest.getSortType());

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        return taskRepository.findAll(specification, pageRequest);
    }

    public TaskEntity getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    public TaskEntity createTask(TaskRequest request) {
        TaskEntity taskEntity = TaskEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .deadline(request.getDeadline())
                .category(request.getCategory())
                .status(TaskStatus.TODO)
                .build();

        TaskEntity savedTask = taskRepository.save(taskEntity);

        taskHistoryService.addHistory(
                savedTask.getId(),
                TaskHistoryAction.CREATED,
                "Task created",
                null,
                null
        );

        return savedTask;
    }

    @Transactional
    public void deleteTask(Long id) {
        TaskEntity taskEntity = getTaskById(id);

        taskHistoryService.addHistory(
                taskEntity.getId(),
                TaskHistoryAction.DELETED,
                "Task deleted",
                null,
                null
        );

        taskRepository.delete(taskEntity);
    }
}
