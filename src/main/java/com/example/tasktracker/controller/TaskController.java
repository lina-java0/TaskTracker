package com.example.tasktracker.controller;

import com.example.tasktracker.dto.TaskRequest;
import com.example.tasktracker.entities.TaskEntity;
import com.example.tasktracker.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

//    @GetMapping
//    public ResponseEntity<List<TaskEntity>> getAllTasks() {
//        return ResponseEntity.ok(taskService.getTasks());
//    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskEntity> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping
    public ResponseEntity<TaskEntity> createTask(@Valid @RequestBody TaskRequest request) {
        TaskEntity createdTaskEntity = taskService.createTask(request);
        return ResponseEntity.status(201).body(createdTaskEntity);
    }

//    @PutMapping("/{id}/complete")
//    public ResponseEntity<Void> completeTask(@PathVariable Long id) {
//        taskService.changeStatus(id);
//        return ResponseEntity.noContent().build();
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
