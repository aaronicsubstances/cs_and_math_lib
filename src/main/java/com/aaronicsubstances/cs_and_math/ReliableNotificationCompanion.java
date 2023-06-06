package com.aaronicsubstances.cs_and_math;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ReliableNotificationCompanion {    
    private boolean externalProceed = false; // requires lock
    private boolean isCurrentlyExecuting = false; // requires lock
    private AtomicBoolean latestCancellationHandle; // requires lock
    private long lastWorkTimestamp; // requires lock
    
    private int workTimeoutSecs = 0;
    private boolean cancelCurrentExecutionOnWorkTimeout = true;
    
    public int getWorkTimeoutSecs() {
        return workTimeoutSecs;
    }
    
    public void setWorkTimeoutSecs(int workTimeoutSecs) {
        this.workTimeoutSecs = workTimeoutSecs;
    }
    
    public boolean isCancelCurrentExecutionOnWorkTimeout() {
        return cancelCurrentExecutionOnWorkTimeout;
    }
    
    public void setCancelCurrentExecutionOnWorkTimeout(boolean cancelCurrentExecutionOnWorkTimeout) {
        this.cancelCurrentExecutionOnWorkTimeout = cancelCurrentExecutionOnWorkTimeout;
    }

    public void doWork(BiConsumer<Throwable, Boolean> cb) {
        cb.accept(null, Boolean.FALSE);
    }

    public boolean doWork() throws Throwable {
        return false;
    }

    public void reportWorkTimeout() {}
    
    public long fetchCurrentTimestamp() {
        return new Date().getTime();
    }

    public void triggerWork() throws Throwable {
        triggerWork(null);
    }

    public void triggerWork(Consumer<Throwable> cb) throws Throwable {
        AtomicBoolean cancellationHandle = null;
        boolean workTimeoutExceeded = false;
        synchronized (this) {
            externalProceed = true;
            if (!isCurrentlyExecuting) {
                isCurrentlyExecuting = true;
                cancellationHandle = new AtomicBoolean();
                latestCancellationHandle = cancellationHandle;
            }
            else {
                if (workTimeoutSecs > 0 && lastWorkTimestamp > 0) {
                    long currentTimestamp = fetchCurrentTimestamp();
                    if ((currentTimestamp - lastWorkTimestamp) >= workTimeoutSecs * 1000) {
                        workTimeoutExceeded = true;
                        
                        // ensure timeout check is not repeated too often.
                        lastWorkTimestamp = 0;
                        
                        if (cancelCurrentExecutionOnWorkTimeout) {
                            if (latestCancellationHandle != null) {
                                latestCancellationHandle.set(true);
                            }
                            
                            // ensure work can be started by future calls to this method.
                            isCurrentlyExecuting = false;
                        }
                    }
                }
            }
        }
        if (cancellationHandle != null) {
            if (cb != null) {
                startWorkLoop(cancellationHandle, cb);
            }
            else {
                startWork(cancellationHandle);
            }
        }
        if (workTimeoutExceeded) {
            try {
                reportWorkTimeout();
            }
            catch (Throwable err) {}
        }
    }
    
    private void startWork(AtomicBoolean cancellationHandle) throws Throwable {
        while (true) {
            synchronized (this) {
                externalProceed = false;
                lastWorkTimestamp = fetchCurrentTimestamp();
            }
            boolean internalProceed = false, errOccured = false;
            try {
                internalProceed = doWork();
            }
            catch (Throwable t) {
                errOccured = true;
                throw t;
            }
            finally {
                synchronized (this) {
                    if (cancellationHandle.get()) {
                        break;
                    }
                    lastWorkTimestamp = 0;
                    if (errOccured || (!externalProceed && !internalProceed)) {
                        isCurrentlyExecuting = false;
                        break;
                    }
                }
            }
        }
    }
    
    private void startWorkLoop(AtomicBoolean cancellationHandle, Consumer<Throwable> cb) {
        synchronized (this) {
            externalProceed = false;
            lastWorkTimestamp = fetchCurrentTimestamp();
        }
        doWork((err, internalProceed) -> {
            boolean continueLoop = true;
            synchronized (this) {
                if (cancellationHandle.get()) {
                    continueLoop = false;
                }
                else {
                    lastWorkTimestamp = 0;
                    if (err != null || (!externalProceed && !internalProceed)) {
                        isCurrentlyExecuting = false;
                        continueLoop = false;
                    }
                }
            }
            if (continueLoop) {
                startWorkLoop(cancellationHandle, cb);
            }
            else {
                cb.accept(err);
            }
        });
    }
}