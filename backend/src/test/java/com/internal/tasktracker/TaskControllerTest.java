package com.internal.tasktracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSearchTasks_ExcludesArchived() throws Exception {
        // Legacy tasks in data.sql (Task 59 and 62) are archived=TRUE.
        // Searching for 'legacy' should return 0 items because archived tasks must be excluded.
        mockMvc.perform(get("/api/tasks?q=legacy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total", is(0)));

        // Unarchived tasks matching 'csv' (Task 17 & 107) should be returned
        mockMvc.perform(get("/api/tasks?q=csv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    public void testSearchTasks_FilterByStatusStrictly() throws Exception {
        // Querying "API" with status=OPEN should only return OPEN tasks containing "API"
        mockMvc.perform(get("/api/tasks?q=api&status=OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].status", everyItem(is("OPEN"))));
    }

    @Test
    public void testSearchTasks_InvalidStatusHandledGracefully() throws Exception {
        // Passing an invalid status string like 'INVALID_STATUS' should not throw 500
        mockMvc.perform(get("/api/tasks?status=INVALID_STATUS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", greaterThan(0)));
    }

    @Test
    public void testSearchTasks_FastResponseWithoutArtificialDelay() throws Exception {
        long start = System.currentTimeMillis();
        mockMvc.perform(get("/api/tasks?q="))
                .andExpect(status().isOk());
        long duration = System.currentTimeMillis() - start;
        // Verify response time is well under 800ms (artificial delay was 1000ms for empty query)
        assert(duration < 800);
    }
}
