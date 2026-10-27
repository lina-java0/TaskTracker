package com.example.tasktracker.controller.task;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.dto.task.request.TaskRequest;
import com.example.tasktracker.dto.task.request.TaskStatusRequest;
import com.example.tasktracker.dto.task.request.TaskUpdateRequest;
import com.example.tasktracker.dto.task.response.ApiResponse;
import com.example.tasktracker.dto.task.response.TaskResponse;
import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.mapper.task.TaskMapper;
import com.example.tasktracker.service.task.TaskService;
import com.example.tasktracker.service.task.TaskStatusService;
import com.example.tasktracker.service.task.TaskUpdateService;
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
    private final TaskMapper taskMapper;

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @ModelAttribute TaskFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(taskService.getTaskResponse(filterRequest, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id
    ) {
        TaskEntity task = taskService.getTaskById(id);

        return ResponseEntity.ok(taskMapper.toResponse(task));
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
                        taskMapper.toResponse(task)
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
                taskMapper.toResponse(updatedTask)
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
                taskMapper.toResponse(updatedTask)
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
}
