package com.taskmanager.config;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TaskRepository taskRepository;

    @Autowired
    public DataInitializer(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only initialize if database is empty
        if (taskRepository.count() == 0) {
            initializeSampleTasks();
        }
    }

    private void initializeSampleTasks() {
        // Create sample tasks for demonstration
        Task task1 = new Task("Complete project documentation", 
                              "Write comprehensive documentation for the task manager project", 
                              TaskStatus.TODO, 
                              LocalDate.now().plusDays(7));

        Task task2 = new Task("Review code", 
                              "Review and approve pending pull requests", 
                              TaskStatus.IN_PROGRESS, 
                              LocalDate.now().plusDays(2));

        Task task3 = new Task("Setup CI/CD pipeline", 
                              "Configure continuous integration and deployment", 
                              TaskStatus.TODO, 
                              LocalDate.now().plusDays(14));

        Task task4 = new Task("Update dependencies", 
                              "Update all project dependencies to latest versions", 
                              TaskStatus.DONE, 
                              LocalDate.now().minusDays(1));

        Task task5 = new Task("Create user manual", 
                              "Write user manual for the application", 
                              TaskStatus.TODO, 
                              LocalDate.now().plusDays(10));

        Task task6 = new Task("Fix authentication bug", 
                              "Resolve issue with user authentication flow", 
                              TaskStatus.IN_PROGRESS, 
                              LocalDate.now().plusDays(1));

        // Save sample tasks
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.save(task4);
        taskRepository.save(task5);
        taskRepository.save(task6);

        System.out.println("Sample tasks have been initialized in the database.");
    }
}