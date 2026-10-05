package com.example.tasktracker.specification;

import com.example.tasktracker.dto.TaskFilterRequest;
import com.example.tasktracker.entities.TaskEntity;
import com.example.tasktracker.enums.TaskSortType;
import org.springframework.data.jpa.domain.Specification;

public class TaskSpecificationBuilder {

    private TaskSpecificationBuilder() {
    }

    public static Specification<TaskEntity> build(TaskFilterRequest filterRequest) {

        Specification<TaskEntity> specification = Specification.where(null);

        if (filterRequest.getSearch() != null && !filterRequest.getSearch().isBlank()) {
            specification = specification.and(
                    TaskSpecifications.titleContains(filterRequest.getSearch()));
        }

        if (filterRequest.getPriority() != null) {
            specification = specification.and(
                    TaskSpecifications.hasPriority(filterRequest.getPriority()));
        }

        if (filterRequest.getCategory() != null) {
            specification = specification.and(
                    TaskSpecifications.hasCategory(filterRequest.getCategory()));
        }

        if (filterRequest.getStatus() != null) {
            specification = specification.and(
                    TaskSpecifications.hasStatus(filterRequest.getStatus()));
        }

        if (filterRequest.getDeadline() != null) {
            specification = specification.and(
                    TaskSpecifications.hasDeadlineOn(filterRequest.getDeadline()));
        }

        if (filterRequest.getDeadlineAfter() != null) {
            specification = specification.and(
                    TaskSpecifications.hasDeadlineAfter(filterRequest.getDeadlineAfter()));
        }

        if (filterRequest.getDeadlineBefore() != null) {
            specification = specification.and(
                    TaskSpecifications.hasDeadlineBefore(filterRequest.getDeadlineBefore()));
        }

        if (Boolean.TRUE.equals(filterRequest.getWithoutDeadline())) {
            specification = specification.and(
                    TaskSpecifications.withoutDeadline());
        }

        TaskSortType sortType = filterRequest.getSortType();

        if (sortType == TaskSortType.PRIORITY_HIGH_TO_LOW) {
            specification = specification.and(
                    TaskSpecifications.priorityOrder(true));
        }

        if (sortType == TaskSortType.PRIORITY_LOW_TO_HIGH) {
            specification = specification.and(
                    TaskSpecifications.priorityOrder(false));
        }

        return specification;
    }
}
