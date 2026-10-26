package com.example.tasktracker.service.task;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.dto.task.request.TaskRequest;
import com.example.tasktracker.dto.task.response.TaskResponse;
import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.enums.*;
import com.example.tasktracker.exception.custom_exceptions.TaskNotFoundException;
import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.repository.task.TaskRepository;
import com.example.tasktracker.service.user.CurrentUserService;
import com.example.tasktracker.service.task_history.TaskHistoryService;
import com.example.tasktracker.specification.TaskSpecificationBuilder;
import com.example.tasktracker.specification.TaskSpecifications;
import com.example.tasktracker.validation.PaginationValidator;
import com.example.tasktracker.validation.validation.TaskAccessValidator;
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
    private final TaskHistoryService taskHistoryService;
    private final CurrentUserService currentUserService;
    private final TaskFilterValidator taskFilterValidator;
    private final PaginationValidator paginationValidator;
    private final TaskAccessValidator taskAccessValidator;

    @Transactional
    public TaskEntity createTask(TaskRequest request) {

        UserEntity owner = currentUserService.getCurrentUser();

        TaskEntity taskEntity = TaskEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .deadline(request.getDeadline())
                .category(request.getCategory())
                .status(TaskStatus.TODO)
                .owner(owner)
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

    @Transactional(readOnly = true)
    public Page<TaskEntity> getTasks(
            TaskFilterRequest filterRequest,
            int page,
            int size) {

        if (filterRequest == null) {
            filterRequest = TaskFilterRequest.builder().build();
        }

        boolean isAdmin = currentUserService.isAdmin();

        paginationValidator.validate(page, size);
        taskAccessValidator.validateAdminFilters(filterRequest, isAdmin);
        taskFilterValidator.validate(filterRequest);

        Specification<TaskEntity> specification =
                TaskSpecificationBuilder.build(filterRequest);

        if (!isAdmin) {
            UserEntity owner = currentUserService.getCurrentUser();

            specification = specification.and
                    (TaskSpecifications.hasOwner(owner));
        }

        Sort sort = taskSortService.getSort(filterRequest.getSortType());
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        return taskRepository.findAll(specification, pageRequest);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTaskResponses(
            TaskFilterRequest filterRequest,
            int page,
            int size) {

        return getTasks(filterRequest, page, size)
                .map(task -> new TaskResponse(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getPriority(),
                        task.getDeadline(),
                        task.getCategory(),
                        task.getCreatedAt(),
                        task.getUpdatedAt(),
                        task.getOwner().getUsername()
                ));
    }

    @Transactional(readOnly = true)
    public TaskEntity getTaskById(Long id) {

        if (currentUserService.isAdmin()) {

            return taskRepository.findById(id)
                    .orElseThrow(TaskNotFoundException::new);

        }

        UserEntity owner = currentUserService.getCurrentUser();

        return taskRepository.findByIdAndOwner(id, owner)
                .orElseThrow(TaskNotFoundException::new);
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
