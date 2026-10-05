package com.example.tasktracker.service;

import com.example.tasktracker.enums.TaskSortType;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TaskSortService {

    public Sort getSort(TaskSortType sortType) {

        if (sortType == null) {
            return Sort.by(
                    Sort.Direction.DESC,
                    "createdAt"
            );
        }

        return switch (sortType) {

            case PRIORITY_HIGH_TO_LOW, PRIORITY_LOW_TO_HIGH ->
                    Sort.unsorted();

            case DEADLINE_ASC ->
                    Sort.by(
                            Sort.Direction.ASC,
                            "deadline"
                    );
            case DEADLINE_DESC ->
                    Sort.by(
                            Sort.Direction.DESC,
                            "deadline"
                    );
            case CREATED_AT_ASC ->
                    Sort.by(
                            Sort.Direction.ASC,
                            "createdAt"
                    );
            case CREATED_AT_DESC ->
                    Sort.by(
                            Sort.Direction.DESC,
                            "createdAt"
                    );
        };
    }
}
