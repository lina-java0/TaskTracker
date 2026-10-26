package com.example.tasktracker.repository.task_history;

import com.example.tasktracker.entities.task_history.TaskHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskHistoryRepository extends JpaRepository<TaskHistoryEntity, Long> {

    List<TaskHistoryEntity> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
