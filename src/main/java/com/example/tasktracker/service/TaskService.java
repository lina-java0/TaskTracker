package com.example.tasktracker.service;

import com.example.tasktracker.dto.*;
import com.example.tasktracker.enums.*;
import com.example.tasktracker.exception.TaskNotFoundException;
import com.example.tasktracker.entities.TaskEntity;
import com.example.tasktracker.repository.TaskRepository;
import com.example.tasktracker.specification.TaskSpecificationBuilder;
import com.example.tasktracker.validation.TaskFilterValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskSortService taskSortService;
    private final TaskFilterValidator taskFilterValidator;

    public List<TaskEntity> getTasks(TaskFilterRequest filterRequest) {
        taskFilterValidator.validate(filterRequest);
        Specification<TaskEntity> specification = TaskSpecificationBuilder.build(filterRequest);
        Sort sort = taskSortService.getSort(filterRequest.getSortType());
        return taskRepository.findAll(specification, sort);
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

        return taskRepository.save(taskEntity);
    }

    @Transactional
    public TaskEntity updateTask(Long id, TaskUpdateRequest request) {
        TaskEntity taskEntity = getTaskById(id);

        taskEntity.setTitle(request.getTitle());
        taskEntity.setDescription(request.getDescription());
        taskEntity.setPriority(request.getPriority());
        taskEntity.setDeadline(request.getDeadline());
        taskEntity.setCategory(request.getCategory());

        return taskEntity;
    }

    @Transactional
    public TaskEntity changeStatus(Long id, TaskStatus status) {
        TaskEntity taskEntity = getTaskById(id);
        taskEntity.setStatus(status);
        return taskEntity;
    }

    @Transactional
    public void deleteTask(Long id) {
        TaskEntity taskEntity = getTaskById(id);
        taskRepository.delete(taskEntity);
    }
}
