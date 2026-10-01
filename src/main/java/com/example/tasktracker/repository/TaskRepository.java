package com.example.tasktracker.repository;

import com.example.tasktracker.entities.TaskEntity;
import org.springframework.data.jpa.repository.*;

public interface TaskRepository extends JpaRepository<TaskEntity, Long>,
                                        JpaSpecificationExecutor<TaskEntity> {
}
