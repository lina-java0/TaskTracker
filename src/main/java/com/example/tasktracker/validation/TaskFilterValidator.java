package com.example.tasktracker.validation;

import com.example.tasktracker.dto.TaskFilterRequest;
import org.springframework.stereotype.Component;

@Component
public class TaskFilterValidator {

    public void validate(TaskFilterRequest filterRequest) {
        if (filterRequest.isWithoutDeadline() && hasDeadlineFilter(filterRequest)) {
            throw new IllegalArgumentException("Filter withoutDeadline cannot be combined with deadline filters");
        }
    }

    private boolean hasDeadlineFilter(TaskFilterRequest filterRequest) {
        return filterRequest.getDeadline() != null
                || filterRequest.getDeadlineAfter() != null
                || filterRequest.getDeadlineBefore() != null;
    }
}
