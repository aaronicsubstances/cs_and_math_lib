package com.aaronicsubstances.cs_and_math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

/*
 * Test
-expected behaviour
--must meet a minimum number of calls when times of trigger are known.
--must never interleave inside the user defined work function.
---setTimeout is used to make interleaving possible.
--test exceptions
--use thread.sleep() and atomic variables to test interleaving and minimum calls
  of both doWork and reportWorkTimeout
*/
public class SetIntervalWorkerTest {

    @Test
    public void testOneOffSyncWork() throws Throwable {
        List<String> callbackLogs = new ArrayList<>();
        SetIntervalWorker instance = new SetIntervalWorker() {
            @Override
            public boolean doWork() {
                callbackLogs.add("3");
                return false;
            }
        };
        instance.triggerWork();

        assertThat(callbackLogs, is(Arrays.asList("3")));
    }

    @Test
    public void testSyncWorkAndInternalPending() throws Throwable {
        List<String> callbackLogs = new ArrayList<>();
        SetIntervalWorker instance = new SetIntervalWorker() {
            @Override
            public boolean doWork() {
                callbackLogs.add("2");
                return callbackLogs.size() < 3;
            }
        };
        instance.triggerWork();

        assertThat(callbackLogs, is(Arrays.asList("2", "2", "2")));
    }

    @Test
    public void testAsyncWorkWithMultipleCallingBack() {
        // arrange
        VirtualEventLoop eventLoop = new VirtualEventLoop();
        List<String> callbackLogs = new ArrayList<>();
        SetIntervalWorker instance = new SetIntervalWorker() {
            @Override
            public void doWork(BiConsumer<Throwable, Boolean> cb) {
                callbackLogs.add("2");
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
        assertThat(actualErrors, is(Arrays.asList("10:null")));
        assertThat(callbackLogs, is(Arrays.asList("2", "2", "2")));
    }

    @Test
    public void testAsyncWorkAndBothInternalAndExternalPending() throws Throwable {
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
        assertThat(actualErrors, is(expectedErrors));
        assertThat(callbackLogs, is(expectedLogs));
    }

    @Test
    public void testAsyncWorkTimeoutWithoutCancelling() throws Throwable {
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
        assertThat(actualErrors, is(expectedErrors));
        assertThat(callbackLogs, is(expectedLogs));
    }

    @Test
    public void testAsyncWorkTimeoutWithCancelling() throws Throwable {
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
        assertThat(actualErrors, is(expectedErrors));
        assertThat(callbackLogs, is(expectedLogs));
    }
}
