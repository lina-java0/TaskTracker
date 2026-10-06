package com.example.tasktracker.service;

import com.example.tasktracker.enums.TaskSortType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskSortServiceTest {

    private final TaskSortService taskSortService = new TaskSortService();

    @ParameterizedTest
    @CsvSource({
            "DEADLINE_ASC,    ASC,  deadline",
            "DEADLINE_DESC,   DESC, deadline",
            "CREATED_AT_ASC,  ASC,  createdAt",
            "CREATED_AT_DESC, DESC, createdAt"
    })
    void shouldReturnCorrectSort(
            TaskSortType sortType,
            Sort.Direction direction,
            String property) {

        Sort sort = taskSortService.getSort(sortType);

        assertEquals(
                Sort.by(direction, property),
                sort
        );
    }

    @ParameterizedTest
    @CsvSource({
            "PRIORITY_HIGH_TO_LOW",
            "PRIORITY_LOW_TO_HIGH"
    })
    void shouldReturnUnsortedForPrioritySort(TaskSortType sortType) {

        Sort sort = taskSortService.getSort(sortType);

        assertEquals(Sort.unsorted(), sort);
    }

    @Test
    void shouldSortByCreatedAtDescWhenSortTypeIsNull() {
        Sort sort = taskSortService.getSort(null);

        assertEquals(
                Sort.by(Sort.Direction.DESC, "createdAt"),
                sort
        );
    }
}
