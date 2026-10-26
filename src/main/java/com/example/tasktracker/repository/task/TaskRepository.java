package com.example.tasktracker.repository.task;

import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.entities.user.UserEntity;
import org.springframework.data.jpa.repository.*;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long>,
                                        JpaSpecificationExecutor<TaskEntity> {

    @EntityGraph(attributePaths = "owner")
    Optional<TaskEntity> findById(Long id);

    @EntityGraph(attributePaths = "owner")
    Optional<TaskEntity> findByIdAndOwner(Long id, UserEntity owner);
}
