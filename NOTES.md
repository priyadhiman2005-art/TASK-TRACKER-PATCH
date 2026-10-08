# Developer Patch Notes

## Summary of Changes
- **Fixed SQL Operator Precedence Bug (`TaskRepository.java`, `search_tasks.sql`, PL/SQL)**: Grouped `(LOWER(title) LIKE :term OR LOWER(description) LIKE :term)` with explicit parentheses. Previously, SQL `AND` precedence leaked archived tasks and ignored status filters.
- **Removed Artificial Controller Latency (`TaskController.java`)**: Removed `Thread.sleep` calculation that blocked request worker threads up to 1000ms.
- **Input Sanitization & Wildcard Escaping (`TaskController.java`, `TaskRepository.java`)**: Handled invalid status enums gracefully (fallback to all tasks), clamped pagination bounds (`page >= 1`, `pageSize <= 100`), escaped SQL wildcards (`%`, `_`), and added `ESCAPE '\\'` clause.
- **Enhanced API & Error Handling (`TaskController.java`, `application.properties`)**: Added `totalPages` metadata, added `@ExceptionHandler` returning JSON errors rather than whitelabel HTML, removed deprecated H2Dialect, and disabled `open-in-view`.
- **Database Index Optimization (`schema.sql`)**: Added composite index on `(archived, status)` and `(created_at DESC)` for index-backed sorting and filtering.
- **Synchronized Oracle PL/SQL Artifact (`task_search_package.sql`)**: Fixed precedence logic and restored missing `archived` column in `TYPE task_record`.
- **Frontend Race Conditions & Debouncing (`useTasks.js`, `App.jsx`, UI components)**: Added request cancellation in cleanup, guaranteed `loading: false` on error, added 300ms search debouncing, reset page to 1 on filter changes, and added accessibility labels and element IDs.
- **Automated Integration Tests (`TaskControllerTest.java`)**: Added 6 MockMvc integration tests verifying correctness, filtering, latency, and pagination.

## What I Chose Not to Change
- Kept controller in-memory slice for pagination rather than switching to Spring Data `Pageable` repository query to preserve the exact API signature and avoid unnecessary architectural churn.

## Biggest Remaining Risk
- `LOWER(...) LIKE %term%` cannot utilize standard B-Tree indexes for leading wildcards. At higher data volumes, this requires H2 full-text search (Lucene) or Elasticsearch.

## AI Tools Used
- Used Gemini 3.8 Flash to audit cross-layer SQL precedence, generate mock handwritten note artifacts, and construct MockMvc regression tests.
