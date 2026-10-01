package com.example.tasktracker.specification;

import com.example.tasktracker.dto.TaskFilterRequest;
import com.example.tasktracker.entities.TaskEntity;
import org.springframework.data.jpa.domain.Specification;

public class TaskSpecificationBuilder {

    public static Specification<TaskEntity> build(TaskFilterRequest filterRequest) {

        Specification<TaskEntity> specification = Specification.where(null);

        if (filterRequest.getPriority() != null) {
            specification = specification.and(TaskSpecifications.hasPriority(filterRequest.getPriority()));
        }

        if (filterRequest.getCategory() != null) {
            specification = specification.and(TaskSpecifications.hasCategory(filterRequest.getCategory()));
        }

        if (filterRequest.getStatus() != null) {
            specification = specification.and(TaskSpecifications.hasStatus(filterRequest.getStatus()));
        }

        if (filterRequest.getDeadline() != null) {
            specification = specification.and(TaskSpecifications.hasDeadlineOn(filterRequest.getDeadline()));
        }

        if (filterRequest.getDeadlineAfter() != null) {
            specification = specification.and(TaskSpecifications.hasDeadlineAfter(filterRequest.getDeadlineAfter()));
        }

        if (filterRequest.getDeadlineBefore() != null) {
            specification = specification.and(TaskSpecifications.hasDeadlineBefore(filterRequest.getDeadlineBefore()));
        }

        if (filterRequest.isWithoutDeadline()) {
            specification = specification.and(TaskSpecifications.withoutDeadline());
        }

        return specification;
    }
}
