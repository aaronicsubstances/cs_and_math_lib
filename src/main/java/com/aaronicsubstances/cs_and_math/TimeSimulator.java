package com.aaronicsubstances.cs_and_math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simulator of events which does not use real time.
 */
public class TimeSimulator {
    private final List<TaskDescriptor> taskQueue = new ArrayList<>();
    private int cancelledTaskCount = 0;
    private int idSeq = 0;
    private long currentTimestamp;

    /**
     * Constructs a new instance with a current virtual timestamp of zero.
     */
    public TimeSimulator() {
    }

    /**
     * Gets the current virtual timestamp value. This value is not related to real time in any way.
     * <p>
     * Note that any time a callback is scheduled for
     * execution, the value of this property will be set to the scheduled time of the callback
     * prior to the execution of the callback. Thus this value does not increase monotonically
     * unless clients always advance time forward.
     * <p>
     * E.g. if one calls only {@link #advanceTimeBy(int)}, then this value will increase monotonically.
     */
    public long getCurrentTimestamp() {
        return currentTimestamp;
    }

    /**
     * Returns the current number of callbacks awaiting execution.
     */
    public int getPendingEventCount() {
        return taskQueue.size() - cancelledTaskCount;
    }

    /**
     * Advances time forward by a given value and executes all pending callbacks and any recursively 
     * scheduled callbacks whose scheduled time do not exceed the current virtual timestamp plus
     * the given value.
     * @param delay the value which when added to the current virtual timestamp will result in
     * a new value for this instance, if this call completes without interference
     */
    public void advanceTimeBy(int delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("negative timeout value: " + delay);
        }
        long newTimestamp;
        newTimestamp = currentTimestamp + delay;
        advanceTimeTo(newTimestamp);
    }
    
    /**
     * Advances time forward or backward to a given value and executes all pending callbacks and
     * any recursively scheduled callbacks whose scheduled time do not exceed given value.
     * @param newTimestamp the new value of current virtual timestamp if this call completes
     * without interference
     */
    public void advanceTimeTo(long newTimestamp) {
        if (newTimestamp < 0) {
            throw new IllegalArgumentException("negative timestamp value: " + newTimestamp);
        }
        triggerActions(newTimestamp);
    }

    /**
     * Work horse of real time simulation up to some virtual timestamp.
     * @param stoppageTimestamp The virtual timestamp at which to stop simulations.
     */
    private void triggerActions(long stoppageTimestamp) {
        // invoke task queue actions starting with tail of queue
        // and stop if item's time is in the future.
        // use tail instead of head since removing at end of array-backed list
        // is faster that from front, because of the shifting required
        while (!taskQueue.isEmpty()) {
            TaskDescriptor earliestTaskDescriptor = taskQueue.get(taskQueue.size() - 1);
            if (!earliestTaskDescriptor.cancelled &&
                    earliestTaskDescriptor.scheduledAt > stoppageTimestamp) {
                break;
            }

            taskQueue.remove(taskQueue.size() - 1);

            if (earliestTaskDescriptor.cancelled) {
                cancelledTaskCount--;
            }
            else {
                currentTimestamp = earliestTaskDescriptor.scheduledAt;
                earliestTaskDescriptor.callback.run();
            }
        }
        currentTimestamp = stoppageTimestamp;
    }

    /**
     * Schedules callback to be run in this instance at the current virtual time, ie "now".
     * If there are already callbacks scheduled "now", the callback will execute after them "now".
     * <p>
     * It is equivalent to invoking {@link #setTimeout(Runnable, int)} with virtual time of zero.
     * @param cb the callback to run
     * @return handle which can be used to cancel timeout request with {@link #clearTimeout(Object)}.
     */
    public Object postCallback(Runnable cb) {
        return setTimeout(cb, 0);
    }

    /**
     * Schedules callback to be run in this instance at a given virtual time if not cancelled.
     * If there are already callbacks scheduled at that time, the callback will execute after
     * them at that time.
     * <p>
     * The callback will only be executed as a result of an ongoing or future call to
     * {@link #advanceTimeBy(int)} or {@link #advanceTimeTo(long)} methods.
     * @param cb the callback to run
     * @param millis the virtual time delay after the current virtual time by which time the callback
     * will be executed
     * @return handle which can be used to cancel timeout request with {@link #clearTimeout(Object)}.
     */
    public Object setTimeout(Runnable cb, int millis) {
        if (cb == null) {
            throw new IllegalArgumentException("null cb");
        }
        if (millis < 0) {
            throw new IllegalArgumentException("negative timeout value: " + millis);
        }
        TaskDescriptor taskDescriptor = new TaskDescriptor();
        taskDescriptor.id = idSeq++;
        taskDescriptor.callback = cb;
        taskDescriptor.scheduledAt = currentTimestamp + millis;
        taskDescriptor.cancelled = false;
        insertIntoSortedTasks(taskDescriptor);
        return taskDescriptor;
    }
    
    private void insertIntoSortedTasks(TaskDescriptor taskDescriptor) {
        // stable sort in reverse order since we will be retrieving from end of list.
        // for speed, leverage already sorted nature of queue, and use inner loop
        // of insertion sort.
        int insertIdx = 0;
        for (int i = taskQueue.size() - 1; i >= 0; i--) {
            if (taskQueue.get(i).scheduledAt > taskDescriptor.scheduledAt) {
                insertIdx = i + 1;
                break;
            }
        }
        taskQueue.add(insertIdx, taskDescriptor);
    }

    /**
     * Used to cancel the execution of a callback scheduled with {@link #setTimeout(Runnable, int)}
     *
     * @param timeoutHandle cancellation handle returned from {@link #setTimeout(Runnable, int)}.
     * No exception is thrown if handle is invalid or if callback execution has already been cancelled.
     */
    public void clearTimeout(Object timeoutHandle) {
        if (timeoutHandle instanceof TaskDescriptor) {
            TaskDescriptor taskDescriptor = (TaskDescriptor)timeoutHandle;
            cancelTask(taskDescriptor.id, taskDescriptor.scheduledAt);
        }
    }

    private void cancelTask(int targetId, long targetScheduledAt) {
        // leverage already sorted nature of queue and
        // use binary search to quickly locate desired id.
        TaskDescriptor searchKey = new TaskDescriptor();
        searchKey.scheduledAt = targetScheduledAt;
        int idxToSearchAround = Collections.binarySearch(taskQueue, searchKey,
            new Comparator<TaskDescriptor>() {
                @Override
                public int compare(TaskDescriptor t1, TaskDescriptor t2) {
                    // reverse order.
                    return Long.compare(t2.scheduledAt, t1.scheduledAt);
                }
            });
        if (idxToSearchAround < 0) {
            return;
        }

        // search forwards and backwards from binary search result for desired id.
        int indexToCancel = -1;
        // search forward
        for (int i = idxToSearchAround; i < taskQueue.size(); i++) {
            TaskDescriptor task = taskQueue.get(i);
            if (task.scheduledAt != targetScheduledAt) {
                break;
            }
            if (task.id == targetId) {
                indexToCancel = i;
                break;
            }
        }
        if (indexToCancel == -1) {
            // search backward
            for (int i = idxToSearchAround - 1; i >= 0; i--) {
                TaskDescriptor task = taskQueue.get(i);
                if (task.scheduledAt != targetScheduledAt) {
                    break;
                }
                if (task.id == targetId) {
                    indexToCancel = i;
                    break;
                }
            }
        }
        if (indexToCancel != -1) {
            TaskDescriptor task = taskQueue.get(indexToCancel);
            if (!task.cancelled) {
                task.cancelled = true;
                cancelledTaskCount++;
            }
        }
    }

    private static class TaskDescriptor {
        public int id;
        public Runnable callback;
        public long scheduledAt;
        public boolean cancelled;
    }
}
