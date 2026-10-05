package com.example.tasktracker.controller;

import com.example.tasktracker.dto.*;
import com.example.tasktracker.entities.TaskEntity;
import com.example.tasktracker.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskUpdateService taskUpdateService;
    private final TaskStatusService taskStatusService;

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @ModelAttribute TaskFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<TaskEntity> tasks = taskService.getTasks(
                filterRequest,
                page,
                size
        );

        Page<TaskResponse> response = tasks.map(this::toResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id
    ) {
        TaskEntity task = taskService.getTaskById(id);

        return ResponseEntity.ok(toResponse(task));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskRequest request
    ) {
        TaskEntity task  = taskService.createTask(request);

        return ResponseEntity
                .status(201)
                .body(new ApiResponse<>(
                        "Task created successfully",
                        toResponse(task)
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest request
    ) {
        TaskEntity updatedTask = taskUpdateService.updateTask(id, request);

        return ResponseEntity.ok(new ApiResponse<>(
                "Task updated successfully",
                toResponse(updatedTask)
        ));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusRequest request
    ) {
        TaskEntity updatedTask = taskStatusService.changeStatus(id, request.getStatus());

        return ResponseEntity.ok(new ApiResponse<>(
                "Task status changed successfully",
                toResponse(updatedTask)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long id
    ) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Task deleted successfully",
                        null
                ));
    }

    private TaskResponse toResponse(TaskEntity task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .deadline(task.getDeadline())
                .category(task.getCategory())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
