package com.example.tasktracker.controller.task_history;

import com.example.tasktracker.dto.task_history.TaskHistoryResponse;
import com.example.tasktracker.service.task_history.TaskHistoryService;
import com.example.tasktracker.service.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskHistoryController {

    private final TaskHistoryService taskHistoryService;

    @GetMapping("/{id}/history")
    public ResponseEntity<List<TaskHistoryResponse>> getHistory(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                taskHistoryService.getHistory(id)
        );
    }
}
