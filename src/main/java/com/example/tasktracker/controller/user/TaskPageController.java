package com.example.tasktracker.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ui")
public class TaskPageController {

    @GetMapping("/tasks")
    public String tasksPage() {
        return "tasks";
    }
}
