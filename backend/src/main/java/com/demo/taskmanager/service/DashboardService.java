package com.demo.taskmanager.service;

import com.demo.taskmanager.domain.entity.Task;
import com.demo.taskmanager.domain.enums.TaskPriority;
import com.demo.taskmanager.domain.enums.TaskStatus;
import com.demo.taskmanager.domain.repository.TaskRepository;
import com.demo.taskmanager.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;

    public DashboardResponse getDashboard(Long userId) {
        List<Task> tasks = taskRepository.findByUserId(userId);

        long totalTodo = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.TODO)
                .count();

        long totalInProgress = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
                .count();

        long totalDone = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .count();

        long totalOverdue = tasks.stream()
                .filter(task -> task.getDueDate() != null
                        && task.getDueDate().isBefore(LocalDate.now())
                        && task.getStatus() != TaskStatus.DONE)
                .count();

        Map<TaskPriority, Long> tasksByPriority = new EnumMap<>(TaskPriority.class);
        for (TaskPriority priority : TaskPriority.values()) {
            tasksByPriority.put(priority, 0L);
        }

        tasks.forEach(task -> tasksByPriority.computeIfPresent(task.getPriority(), (priority, total) -> total + 1));

        Map<String, Long> totalByPriority = new java.util.LinkedHashMap<>();
        for (TaskPriority priority : TaskPriority.values()) {
            totalByPriority.put(priority.name(), tasksByPriority.get(priority));
        }

        return DashboardResponse.builder()
                .totalTasks(tasks.size())
                .totalTodo(totalTodo)
                .totalInProgress(totalInProgress)
                .totalDone(totalDone)
                .totalOverdue(totalOverdue)
                .totalByPriority(totalByPriority)
                .build();
    }
}
