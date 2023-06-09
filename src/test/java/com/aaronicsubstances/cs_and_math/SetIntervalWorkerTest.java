package com.aaronicsubstances.cs_and_math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

public class SetIntervalWorkerTest {

    @Test
    public void testSyncWorkOneOff() throws Throwable {
        // arrange
        int[] callCount = new int[1];
        SetIntervalWorker instance = new SetIntervalWorker() {
            @Override
            public boolean doWork() {
                callCount[0]++;
                return false;
            }
        };
        
        // act
        instance.triggerWork();

        // assert
        assertEquals(callCount[0], 1);
    }

    @Test
    public void testSyncWorkAndInternalPending() throws Throwable {
        // arrange
        int[] callCount = new int[1];
        SetIntervalWorker instance = new SetIntervalWorker() {
            @Override
            public boolean doWork() {
                callCount[0]++;
                return callCount[0] < 3;
            }
        };
        
        // act
        instance.triggerWork();

        // assert
        assertEquals(callCount[0], 3);
    }

    @Test
    public void testSyncWorkAndExternalPending() throws Throwable {
        // arrange
        int[] callCount = new int[1];
        SetIntervalWorker instance = new SetIntervalWorker() {
            @Override
            public boolean doWork() throws Throwable {
                callCount[0]++;
                if (callCount[0] < 10) {
                    triggerWork();  // to set externalPending to true.
                }
                return false;
            }
        };
        
        // act
        instance.triggerWork();

        // assert
        assertEquals(callCount[0], 10);
    }

    @Test
    public void testSyncWorkTimeoutWithCancelling() throws Throwable {
        // arrange
        AtomicInteger callCount = new AtomicInteger();
        AtomicInteger virtualTimeSeed = new AtomicInteger(10);
        AtomicBoolean signaller = new AtomicBoolean();
        AtomicInteger reportCount = new AtomicInteger();
        SetIntervalWorker instance = new SetIntervalWorker() {
            @Override
            public boolean doWork() throws Throwable {
                if (callCount.incrementAndGet() == 1) {
                    synchronized (this) {
                        while (!signaller.get()) {
                            wait();
                        }
                    }
                    return true; // without proper cancellation 
                                 // this can result in endless looping
                }
                return false;
            }
            @Override
            public long fetchCurrentTimestamp() {
                // increment ensures timeout will be triggered.
                return virtualTimeSeed.getAndAdd(5000);
            }
            @Override
            public void reportWorkTimeout(long t) {
                reportCount.incrementAndGet();
            }
        };
        instance.setWorkTimeoutSecs(3);

        // act by spawning a thread to race with
        // current thread, such that the first to call
        // triggerWork will block, and the second should
        // unblock it.
        Thread th = new Thread() {
            @Override
            public void run() {
                try {
                    instance.triggerWork();
                    signaller.set(true);
                    synchronized (instance) {
                        instance.notify();
                    }
                }
                catch (Throwable err) {
                    throw new RuntimeException(err);
                }
            }
        };
        th.start();
        instance.triggerWork();
        signaller.set(true);
        synchronized (instance) {
            instance.notify();
        }
        
        // should not be blocked.
        th.join();
        instance.triggerWork();
        
        // assert
        assertEquals(callCount.get(), 2);
        assertEquals(reportCount.get(), 1);
    }

    @Test
    public void testSyncWorkAndErrorHandling() throws Throwable {
        // arrange
        int[] callCount = new int[1];
        SetIntervalWorker instance = new SetIntervalWorker() {
            @Override
            public boolean doWork() throws Throwable {
                callCount[0]++;
                if (callCount[0] == 1) {
                    triggerWork(); // to set externalPending to true.
                    throw new RuntimeException("direct");
                }
                if (callCount[0] == 1 || callCount[0] > 5) {
                    throw new RuntimeException("direct");
                }
                return callCount[0] < 3;
            }
        };
        String exception = null;

        // act/assert
        exception = null;
        try {
            instance.triggerWork();
        }
        catch (Throwable err) {
            exception = err.getMessage();
        }
        assertEquals(callCount[0], 1);
        assertEquals(exception, "direct");

        exception = null;
        try {
            instance.triggerWork();
        }
        catch (Throwable err) {
            exception = err.getMessage();
        }
        assertEquals(callCount[0], 3);
        assertEquals(exception, null);

        // act/assert
        exception = null;
        try {
            instance.triggerWork();
        }
        catch (Throwable err) {
            exception = err.getMessage();
        }
        assertEquals(callCount[0], 4);
        assertEquals(exception, null);

        // act/assert
        exception = null;
        try {
            instance.triggerWork();
        }
        catch (Throwable err) {
            exception = err.getMessage();
        }
        assertEquals(callCount[0], 5);
        assertEquals(exception, null);

        // act/assert
        exception = null;
        try {
            instance.triggerWork();
        }
        catch (Throwable err) {
            exception = err.getMessage();
        }
        assertEquals(callCount[0], 6);
        assertEquals(exception, "direct");

        // act/assert
        exception = null;
        try {
            instance.triggerWork();
        }
        catch (Throwable err) {
            exception = err.getMessage();
        }
        assertEquals(callCount[0], 7);
        assertEquals(exception, "direct");
    }

    @Test
    public void testAsyncWorkWithMultipleCallingBack() {
        // arrange
        VirtualEventLoop eventLoop = new VirtualEventLoop();
        List<String> callbackLogs = new ArrayList<>();
        SetIntervalWorker instance = new SetIntervalWorker() {
            @Override
            public void doWork(BiConsumer<Throwable, Boolean> cb) {
                callbackLogs.add("" + eventLoop.getCurrentTimestamp());
                Runnable timeoutCb = () -> {
                    cb.accept(null, callbackLogs.size() < 3);
                };
                eventLoop.setTimeout(timeoutCb, 2);
                // repeat
                eventLoop.setTimeout(timeoutCb, 2);
                eventLoop.setTimeout(timeoutCb, 7);
            }
        };
        List<String> actualErrors = new ArrayList<>();
        eventLoop.setTimeout(() -> {
            instance.triggerWork(err -> {
                actualErrors.add(eventLoop.getCurrentTimestamp() + ":" + err);
            });
        }, 4);

        // act
        eventLoop.advanceTimeTo(100);

        // assert
        assertThat(callbackLogs, is(Arrays.asList("4", "6", "8")));
        assertThat(actualErrors, is(Arrays.asList("10:null")));
    }

    @Test
    public void testAsyncWorkAndBothInternalAndExternalPending() {
        // arrange
        VirtualEventLoop eventLoop = new VirtualEventLoop();
        List<String> callbackLogs = new ArrayList<>();
        List<String> expectedLogs = Arrays.asList(
            "3:0", "11:0", "11:2", "19:2", "19:4", "27:4", "27:6", "35:6",
                "69:8", "77:8", "77:10", "85:10");
        SetIntervalWorker instance = new SetIntervalWorker() {
            @Override
            public void doWork(BiConsumer<Throwable, Boolean> cb) {
                callbackLogs.add(eventLoop.getCurrentTimestamp() + ":" + callbackLogs.size());
                eventLoop.setTimeout(() -> {
                    int afterPossibleInterleavingSize = callbackLogs.size() - 1;
                    callbackLogs.add(eventLoop.getCurrentTimestamp() + ":" +
                        afterPossibleInterleavingSize);
                    cb.accept(null, callbackLogs.size() < 2);
                }, 8);
            }
        };
        int[] times = new int[]{ 76, 3, 4, 26, 12, 69, 15 };
        List<String> actualErrors = new ArrayList<>();
        List<String> expectedErrors = Arrays.asList("4:null", "12:null",
            "15:null", "26:null", "35:null", "76:null", "85:null");
        for (int time : times) {
            eventLoop.setTimeout(() -> {
                instance.triggerWork(err -> {
                    actualErrors.add(eventLoop.getCurrentTimestamp() + ":" + err);
                });
            }, time);
        }

        // act
        eventLoop.advanceTimeTo(100);
        
        // assert
        assertThat(callbackLogs, is(expectedLogs));
        assertThat(actualErrors, is(expectedErrors));
    }

    @Test
    public void testAsyncWorkTimeoutWithoutCancelling() {
        // arrange
        VirtualEventLoop eventLoop = new VirtualEventLoop();
        List<String> callbackLogs = new ArrayList<>();
        List<String> expectedLogs = Arrays.asList(
            "11:0", "2011:r:11", "3011:1", "3011:3", "5500:r:3011", "6011:4",
            "6011:6", "9011:6");
        SetIntervalWorker instance = new SetIntervalWorker() {
            @Override
            public void doWork(BiConsumer<Throwable, Boolean> cb) {
                callbackLogs.add(eventLoop.getCurrentTimestamp() + ":" + callbackLogs.size());
                eventLoop.setTimeout(() -> {
                    int afterPossibleInterleavingSize = callbackLogs.size() - 1;
                    callbackLogs.add(eventLoop.getCurrentTimestamp() + ":" +
                        afterPossibleInterleavingSize);
                    cb.accept(null, eventLoop.getCurrentTimestamp() < 3000);
                }, 3000);
            }
            @Override
            public long fetchCurrentTimestamp() {
                return eventLoop.getCurrentTimestamp();
            }
            @Override
            public void reportWorkTimeout(long t) {
                callbackLogs.add(eventLoop.getCurrentTimestamp() + ":" +
                    "r:" + t);
            }
        };
        instance.setCancelCurrentExecutionOnWorkTimeout(false);
        instance.setWorkTimeoutSecs(2);
        int[] times = new int[]{ 60, 11, 2005, 2011, 1940, 2100, 3000, 4000, 5500 };
        List<String> actualErrors = new ArrayList<>();
        List<String> expectedErrors = Arrays.asList("60:null",
            "1940:null", "2005:null", "2011:null", "2100:null", "3000:null",
            "4000:null", "5500:null", "9011:null");
        for (int time : times) {
            eventLoop.setTimeout(() -> {
                instance.triggerWork(err -> {
                    actualErrors.add(eventLoop.getCurrentTimestamp() + ":" + err);
                });
            }, time);
        }

        // act
        eventLoop.advanceTimeTo(10_000);
        
        // assert
        assertThat(callbackLogs, is(expectedLogs));
        assertThat(actualErrors, is(expectedErrors));
    }

    @Test
    public void testAsyncWorkTimeoutWithCancelling() {
        // arrange
        VirtualEventLoop eventLoop = new VirtualEventLoop();
        List<String> callbackLogs = new ArrayList<>();
        List<String> expectedLogs = Arrays.asList(
            "11:0", "2011:r:11", "2100:2", "3011:2", "4500:r:2100", "5100:4" );
        SetIntervalWorker instance = new SetIntervalWorker() {
            @Override
            public void doWork(BiConsumer<Throwable, Boolean> cb) {
                callbackLogs.add(eventLoop.getCurrentTimestamp() + ":" + callbackLogs.size());
                eventLoop.setTimeout(() -> {
                    int afterPossibleInterleavingSize = callbackLogs.size() - 1;
                    callbackLogs.add(eventLoop.getCurrentTimestamp() + ":" +
                        afterPossibleInterleavingSize);
                    cb.accept(null, eventLoop.getCurrentTimestamp() < 3000);
                }, 3000);
            }
            @Override
            public long fetchCurrentTimestamp() {
                return eventLoop.getCurrentTimestamp();
            }
            @Override
            public void reportWorkTimeout(long t) {
                callbackLogs.add(eventLoop.getCurrentTimestamp() + ":" +
                    "r:" + t);
            }
        };
        instance.setWorkTimeoutSecs(2);
        int[] times = new int[]{ 60, 11, 2005, 2011, 1940, 2100, 3000, 4500 };
        List<String> actualErrors = new ArrayList<>();
        List<String> expectedErrors = Arrays.asList("60:null",
            "1940:null", "2005:null", "2011:null", "3000:null", "3011:null",
            "4500:null", "5100:null");
        for (int time : times) {
            eventLoop.setTimeout(() -> {
                instance.triggerWork(err -> {
                    actualErrors.add(eventLoop.getCurrentTimestamp() + ":" + err);
                });
            }, time);
        }

        // act
        eventLoop.advanceTimeTo(10_000);
        
        // assert
        assertThat(callbackLogs, is(expectedLogs));
        assertThat(actualErrors, is(expectedErrors));
    }

    @Test
    public void testAsyncWorkAndErrorHandling() {
        // arrange
        VirtualEventLoop eventLoop = new VirtualEventLoop();
        List<String> callbackLogs = new ArrayList<>();
        List<String> expectedLogs = Arrays.asList(
            "10", "14", "18", "23", "30", "45" );
        SetIntervalWorker instance = new SetIntervalWorker() {
            @Override
            public void doWork(BiConsumer<Throwable, Boolean> cb) {
                if (callbackLogs.size() > 5) {
                    throw new RuntimeException("direct");
                }
                callbackLogs.add(eventLoop.getCurrentTimestamp() + "");
                eventLoop.setTimeout(() -> {
                    if (callbackLogs.size() > 5) {
                        cb.accept(new RuntimeException("indirect"), true);
                    }
                    else {
                        cb.accept(null, eventLoop.getCurrentTimestamp() < 20);
                    }
                }, 4);
            }
            @Override
            public long fetchCurrentTimestamp() {
                return eventLoop.getCurrentTimestamp();
            }
        };
        int[] times = new int[]{ 10, 12, 17, 23, 30, 45, 60 };
        List<String> actualErrors = new ArrayList<>();
        List<String> expectedErrors = Arrays.asList(
            "12:null", "17:null", "22:null", "27:null", "34:null",
            "49:indirect", "60:direct");
        for (int time : times) {
            eventLoop.setTimeout(() -> {
                instance.triggerWork(err -> {
                    actualErrors.add(eventLoop.getCurrentTimestamp() + ":" +
                        (err != null ? err.getMessage() : "null"));
                });
            }, time);
        }

        // act
        eventLoop.advanceTimeTo(100);

        // assert
        assertThat(callbackLogs, is(expectedLogs));
        assertThat(actualErrors, is(expectedErrors));
    }
}
