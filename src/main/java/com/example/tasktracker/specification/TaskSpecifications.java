package com.example.tasktracker.specification;

import com.example.tasktracker.entities.TaskEntity;
import com.example.tasktracker.enums.Category;
import com.example.tasktracker.enums.Priority;
import com.example.tasktracker.enums.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<TaskEntity> hasPriority(Priority priority) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("priority"), priority);
    }

    public static Specification<TaskEntity> hasCategory(Category category) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<TaskEntity> hasStatus(TaskStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<TaskEntity> withoutDeadline() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNull(root.get("deadline"));
    }

    public static Specification<TaskEntity> hasDeadline() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNotNull(root.get("deadline"));
    }

    public static Specification<TaskEntity> hasDeadlineOn(LocalDate date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("deadline"), date);
    }

    public static Specification<TaskEntity> hasDeadlineAfter(LocalDate date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("deadline"), date);
    }

    public static Specification<TaskEntity> hasDeadlineBefore(LocalDate date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("deadline"), date);
    }
}
