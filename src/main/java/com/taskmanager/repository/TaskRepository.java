package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find tasks by status
    List<Task> findByStatus(TaskStatus status);

    // Find tasks by status ordered by due date
    List<Task> findByStatusOrderByDueDateAsc(TaskStatus status);

    // Find all tasks ordered by status and due date
    @Query("SELECT t FROM Task t ORDER BY t.status, t.dueDate ASC NULLS LAST")
    List<Task> findAllOrderByStatusAndDueDate();

    // Find overdue tasks (due date is before today and status is not DONE)
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.status != :doneStatus")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDate currentDate, @Param("doneStatus") TaskStatus doneStatus);

    // Find tasks due today
    List<Task> findByDueDate(LocalDate dueDate);

    // Find tasks by title containing (case insensitive)
    List<Task> findByTitleContainingIgnoreCase(String title);

    // Count tasks by status
    long countByStatus(TaskStatus status);
}