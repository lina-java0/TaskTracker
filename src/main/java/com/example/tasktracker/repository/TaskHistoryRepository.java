package com.example.tasktracker.repository;

import com.example.tasktracker.entities.TaskHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskHistoryRepository extends JpaRepository<TaskHistoryEntity, Long> {

    List<TaskHistoryEntity> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
