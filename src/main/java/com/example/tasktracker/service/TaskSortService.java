package com.example.tasktracker.service;

import com.example.tasktracker.enums.TaskSortType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
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
            case PRIORITY_LOW_TO_HIGH ->
                    JpaSort.unsafe(
                            "CASE " +
                                    "WHEN priority = 'LOW' THEN 1 " +
                                    "WHEN priority = 'MEDIUM' THEN 2 " +
                                    "WHEN priority = 'HIGH' THEN 3 " +
                                    "END"
                    );
            case PRIORITY_HIGH_TO_LOW ->
                    JpaSort.unsafe(
                            "CASE " +
                                    "WHEN priority = 'HIGH' THEN 1 " +
                                    "WHEN priority = 'MEDIUM' THEN 2 " +
                                    "WHEN priority = 'LOW' THEN 3 " +
                                    "END"
                    );
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
