package com.example.tasktracker.validation.task_validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.enums.TaskSortType;
import com.example.tasktracker.exception.custom_exceptions.ForbiddenOperationException;
import org.springframework.stereotype.Component;

@Component
public class TaskAccessValidator {

    public void validateAdminFilters(
            TaskFilterRequest filterRequest,
            boolean isAdmin
    ) {

        if (isAdmin) {
            return;
        }

        if (filterRequest.getOwnerUsername() != null
                && !filterRequest.getOwnerUsername().isBlank()) {

            throw new ForbiddenOperationException();
        }

        TaskSortType sortType = filterRequest.getSortType();

        if (sortType == TaskSortType.OWNER_USERNAME_ASC
                || sortType == TaskSortType.OWNER_USERNAME_DESC) {

            throw new ForbiddenOperationException();
        }
    }
}
