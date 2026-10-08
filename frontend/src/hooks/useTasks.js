import { useState, useEffect, useCallback } from 'react';
import { fetchTasks } from '../api';

export function useTasks(query, status, page, pageSize) {
  const [tasks, setTasks] = useState([]);
  const [total, setTotal] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    let isCancelled = false;
    setLoading(true);
    setError(null);

    fetchTasks({ query, status, page, pageSize })
      .then((data) => {
        if (!isCancelled) {
          setTasks(data.items);
          setTotal(data.total);
          setTotalPages(data.totalPages ?? Math.ceil(data.total / pageSize));
          setLoading(false);
        }
      })
      .catch((err) => {
        if (!isCancelled) {
          setError(err.message);
          setTasks([]);
          setTotal(0);
          setTotalPages(0);
          setLoading(false);
        }
      });

    return () => {
      isCancelled = true;
    };
  }, [query, status, page, pageSize]);

  return { tasks, total, totalPages, loading, error };
}
