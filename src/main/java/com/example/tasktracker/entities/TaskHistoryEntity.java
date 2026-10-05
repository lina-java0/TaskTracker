package com.example.tasktracker.entities;

import com.example.tasktracker.enums.TaskHistoryAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(Types.VARCHAR)
    @Column(nullable = false, length = 50)
    private TaskHistoryAction action;

    @Column(nullable = false, length = 600)
    private String description;

    @Column(name = "old_value", length = 600)
    private String oldValue;

    @Column(name = "new_value", length = 600)
    private String newValue;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
