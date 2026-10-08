# Developer Patch Notes

## Summary of Changes
- **Fixed SQL Operator Precedence Bug (`TaskRepository.java`, `search_tasks.sql`, PL/SQL)**: Added missing parentheses around `(LOWER(title) LIKE :term OR LOWER(description) LIKE :term)`. Previously, SQL operator precedence (`AND` > `OR`) caused archived tasks to be returned and bypassed status filters when title matched search terms.
- **Removed Artificial Controller Latency (`TaskController.java`)**: Removed `Thread.sleep(queryWeight)` delay calculation which caused up to 1-second lag per API call on short or empty queries.
- **Handled Invalid Status Filters (`TaskController.java`)**: Wrapped `TaskStatus.valueOf(...)` in a `try-catch` block to handle invalid or unexpected status parameters gracefully instead of crashing with HTTP 500.
- **Sanitized Pagination & Bounds (`TaskController.java`)**: Clamped `page` (minimum 1) and `pageSize` (1–100) parameters to prevent negative index calculations.
- **Prevented Frontend Race Conditions & Hanging Loading State (`useTasks.js`)**: Introduced cancellation flags in `useEffect` cleanup to ignore out-of-order stale requests, reset `error` state on fetch, and ensured `loading` state is cleared when errors occur.
- **Added Search Debouncing & Page Reset (`App.jsx`)**: Implemented a 300ms debounce on search inputs and ensured `page` resets to 1 whenever query or filter changes.
- **Added Integration Test Suite (`TaskControllerTest.java`, `pom.xml`)**: Added `spring-boot-starter-test` and integration tests covering archived exclusion, strict status filtering, error tolerance, and execution speed.

## What I Chose Not to Change
- **In-Memory Pagination to DB Pageable**: Kept JPA query return as `List<Task>` rather than refactoring to Spring Data `Pageable` to maintain exact contract simplicity within the 90-minute timebox.

## Biggest Remaining Risk
- **Unindexed LIKE '%term%' Queries**: `LOWER(...) LIKE %term%` triggers full table scans on large datasets. Future work should introduce DB full-text indexes or Elasticsearch.

## AI Tools Used
- Used Gemini 3.6 Flash to audit cross-layer SQL logic, draft React debouncing/cancellation hooks, and construct MockMvc integration tests.
