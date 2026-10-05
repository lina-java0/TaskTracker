package com.example.tasktracker.specification;

import com.example.tasktracker.entities.TaskEntity;
import com.example.tasktracker.enums.Category;
import com.example.tasktracker.enums.Priority;
import com.example.tasktracker.enums.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

public class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<TaskEntity> titleContains(String text) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + text.toLowerCase(Locale.ROOT) + "%");
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

    public static Specification<TaskEntity> priorityOrder(boolean highToLow) {
        return (root, query, criteriaBuilder) -> {

            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                return criteriaBuilder.conjunction();
            }

            var priorityOrder = criteriaBuilder
                    .selectCase(root.get("priority"))
                    .when(Priority.HIGH, 1)
                    .when(Priority.MEDIUM, 2)
                    .when(Priority.LOW, 3)
                    .otherwise(4);

            if (highToLow) {
                query.orderBy(criteriaBuilder.asc(priorityOrder));
            } else {
                query.orderBy(criteriaBuilder.desc(priorityOrder));
            }

            return criteriaBuilder.conjunction();
        };
    }
}
