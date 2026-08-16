package com.demo.taskmanager.service;

import com.demo.taskmanager.domain.entity.Task;
import com.demo.taskmanager.domain.entity.User;
import com.demo.taskmanager.domain.enums.TaskPriority;
import com.demo.taskmanager.domain.enums.TaskStatus;
import com.demo.taskmanager.domain.repository.TaskRepository;
import com.demo.taskmanager.dto.DashboardResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboard_shouldAggregateStatusPriorityAndOverdueCounts() {
        User user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@test.com")
                .passwordHash("hash")
                .build();

        List<Task> tasks = List.of(
                task("Plan sprint", TaskStatus.TODO, TaskPriority.HIGH, LocalDate.now().minusDays(2), user),
                task("Review PR", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now().plusDays(1), user),
                task("Deploy release", TaskStatus.DONE, TaskPriority.HIGH, LocalDate.now().minusDays(1), user),
                task("Refine backlog", TaskStatus.TODO, TaskPriority.LOW, null, user)
        );

        when(taskRepository.findByUserId(1L)).thenReturn(tasks);

        DashboardResponse response = dashboardService.getDashboard(1L);

        assertThat(response.getTotalTasks()).isEqualTo(4);
        assertThat(response.getTotalTodo()).isEqualTo(2);
        assertThat(response.getTotalInProgress()).isEqualTo(1);
        assertThat(response.getTotalDone()).isEqualTo(1);
        assertThat(response.getTotalOverdue()).isEqualTo(1);
        assertThat(response.getTotalByPriority()).containsEntry(TaskPriority.LOW.name(), 1L);
        assertThat(response.getTotalByPriority()).containsEntry(TaskPriority.MEDIUM.name(), 1L);
        assertThat(response.getTotalByPriority()).containsEntry(TaskPriority.HIGH.name(), 2L);
    }

    @Test
    void getDashboard_shouldFillMissingPrioritiesWithZero() {
        User user = User.builder()
                .id(2L)
                .name("Bob")
                .email("bob@test.com")
                .passwordHash("hash")
                .build();

        when(taskRepository.findByUserId(2L)).thenReturn(List.of(
                task("Single task", TaskStatus.TODO, TaskPriority.MEDIUM, LocalDate.now().plusDays(3), user)
        ));

        DashboardResponse response = dashboardService.getDashboard(2L);

        assertThat(response.getTotalByPriority()).containsEntry(TaskPriority.LOW.name(), 0L);
        assertThat(response.getTotalByPriority()).containsEntry(TaskPriority.MEDIUM.name(), 1L);
        assertThat(response.getTotalByPriority()).containsEntry(TaskPriority.HIGH.name(), 0L);
    }

    private Task task(String title, TaskStatus status, TaskPriority priority, LocalDate dueDate, User user) {
        return Task.builder()
                .title(title)
                .status(status)
                .priority(priority)
                .dueDate(dueDate)
                .user(user)
                .build();
    }
}
