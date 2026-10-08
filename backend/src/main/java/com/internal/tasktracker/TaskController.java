package com.internal.tasktracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // Normalize query input and escape SQL wildcard characters
        String query = q == null ? "" : q.trim();
        String escapedQuery = query.toLowerCase()
                .replace("%", "\\%")
                .replace("_", "\\_");
        String searchTerm = "%" + escapedQuery + "%";

        // Parse status filter safely
        String normalizedStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                normalizedStatus = TaskStatus.valueOf(status.trim().toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                // Invalid status string passed (e.g. 'ALL') — default to null to match all statuses cleanly
                normalizedStatus = null;
            }
        }

        // Sanitize pagination parameters
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, Math.min(100, pageSize));

        log.info("Search: q=\"{}\" status={} page={} pageSize={}",
                query, normalizedStatus, safePage, safePageSize);

        List<Task> allResults = taskRepository.searchTasks(searchTerm, normalizedStatus);

        int start = (safePage - 1) * safePageSize;
        int end = Math.min(start + safePageSize, allResults.size());
        List<Task> pageResults = (start < allResults.size())
                ? allResults.subList(start, end)
                : Collections.emptyList();

        int totalPages = (int) Math.ceil((double) allResults.size() / safePageSize);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", pageResults);
        response.put("total", allResults.size());
        response.put("page", safePage);
        response.put("pageSize", safePageSize);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }

    // Global handler — return clean JSON instead of Spring whitelabel HTML on unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("Unhandled exception in TaskController", ex);
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Internal server error");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
