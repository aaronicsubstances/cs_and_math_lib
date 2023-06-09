package com.aaronicsubstances.cs_and_math;

import java.util.Date;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Generic solution for repetitive timer tasks (such as NodeJS setInterval) to
 * avoid interleaving of task executions.
 */
public class SetIntervalWorker {
    // these variables require synchronized access
    private boolean externalProceed = false;
    private boolean isCurrentlyExecuting = false;
    private boolean[] latestCancellationHandle; // mutable boolean
    private long lastWorkTimestamp;

    // these fields back user-defined properties
    private int workTimeoutSecs = 3600;
    private boolean cancelCurrentExecutionOnWorkTimeout = true;

    /**
     * Gets maximum time period for interval work. Defaults to 1 hour.
     */
    public int getWorkTimeoutSecs() {
        return workTimeoutSecs;
    }

    public void setWorkTimeoutSecs(int workTimeoutSecs) {
        this.workTimeoutSecs = workTimeoutSecs;
    }

    /**
     * Gets whether any ongoing interval work
     */
    public boolean isCancelCurrentExecutionOnWorkTimeout() {
        return cancelCurrentExecutionOnWorkTimeout;
    }

    public void setCancelCurrentExecutionOnWorkTimeout(boolean cancelCurrentExecutionOnWorkTimeout) {
        this.cancelCurrentExecutionOnWorkTimeout = cancelCurrentExecutionOnWorkTimeout;
    }

    /**
     * Hook for subclasses to perform interval work.
     *
     * @param cb callback which should be called to indicate end of interval work.
     * It takes 2 parameters: the first indicates any error which occured, and the
     * second indicates whether interval work should be triggered again and immediately
     * without interval delay.
     */
    protected void doWork(BiConsumer<Throwable, Boolean> cb) {
        cb.accept(null, Boolean.FALSE);
    }

    /**
     * Synchronous version of doWork(cb)
     */
    protected boolean doWork() throws Throwable {
        return false;
    }

    /**
     * Hook for subclasses to log instances of doWork() calls exceeding their time outs.
     *
     * @params timestamp indicates the time at which work was started.
     */
    protected void reportWorkTimeout(long timestamp) {}

    /**
     * Exposed for overriding with virtual timestamps during test.
     */
    long fetchCurrentTimestamp() {
        return new Date().getTime();
    }

    public void triggerWork() throws Throwable {
        boolean[] cancellationHandle = triggerUpdates();
        if (cancellationHandle != null) {
            startWork(cancellationHandle);
        }
    }

    public void triggerWork(Consumer<Throwable> cb) {
        Objects.requireNonNull(cb, "callback is null");
        boolean[] cancellationHandle = triggerUpdates();
        if (cancellationHandle != null) {
            startWorkLoop(cancellationHandle, cb);
        }
        else {
            cb.accept(null);
        }
    }

    private void startWorkLoop(boolean[] cancellationHandle, Consumer<Throwable> cb) {
        loopPreUpdates();
        boolean[] multiCallbackProtection = new boolean[1];
        try {
            doWork((err, internalProceed) -> {
                if (multiCallbackProtection[0]) {
                    return;
                }
                multiCallbackProtection[0] = true;
                boolean continueLoop = loopPostUpdates(err != null, internalProceed,
                    cancellationHandle);
                if (continueLoop) {
                    startWorkLoop(cancellationHandle, cb);
                }
                else {
                    cb.accept(err);
                }
            });
        }
        catch (Throwable err) {
            loopPostUpdates(true, false, cancellationHandle);
            cb.accept(err);
        }
    }

    private void startWork(boolean[] cancellationHandle) throws Throwable {
        boolean continueLoop = true;
        while (continueLoop) {
            loopPreUpdates();
            boolean internalProceed = false, errOccured = false;
            try {
                internalProceed = doWork();
            }
            catch (Throwable err) {
                errOccured = true;
                throw err;
            }
            finally {
                continueLoop = loopPostUpdates(errOccured, internalProceed,
                    cancellationHandle);
            }
        }
    }

    private boolean[] triggerUpdates() {
        boolean[] cancellationHandle = null;
        long pendingWorkTimestamp = 0;
        synchronized (this) {
            externalProceed = true;
            if (isCurrentlyExecuting) {
                if (workTimeoutSecs > 0 && lastWorkTimestamp > 0) {
                    long currentTimestamp = fetchCurrentTimestamp();
                    if ((currentTimestamp - lastWorkTimestamp) >= workTimeoutSecs * 1000) {
                        pendingWorkTimestamp = lastWorkTimestamp;
                        
                        // ensure timeout check is not repeated too often during work timeout.
                        lastWorkTimestamp = 0;
                        
                        if (cancelCurrentExecutionOnWorkTimeout) {
                            if (latestCancellationHandle != null) {
                                latestCancellationHandle[0] = true;
                            }

                            // ensure interval work can be resumed in the future.
                            isCurrentlyExecuting = false;
                        }
                    }
                }
            }
            else {
                isCurrentlyExecuting = true;
                cancellationHandle = new boolean[1];
                latestCancellationHandle = cancellationHandle;
            }
        }
        if (pendingWorkTimestamp > 0) {
            try {
                reportWorkTimeout(pendingWorkTimestamp);
            }
            catch (Throwable ignore) {}
        }
        return cancellationHandle;
    }

    private synchronized void loopPreUpdates() {
        externalProceed = false; // also prevents endless looping if there is no error.
        lastWorkTimestamp = fetchCurrentTimestamp();
    }

    private synchronized boolean loopPostUpdates(
            boolean errOccured, boolean internalProceed,
            boolean[] cancellationHandle) {
        if (cancellationHandle[0]) {
            return false;
        }
        else {
            lastWorkTimestamp = 0;
            if (errOccured || (!externalProceed && !internalProceed)) {
                cancellationHandle[0] = true;
                isCurrentlyExecuting = false;
                return false;
            }
            else {
                return true;
            }
        }
    }
}
