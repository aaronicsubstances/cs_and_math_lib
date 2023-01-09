package com.aaronicsubstances.cs_and_math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

public class TimeSimulatorTest {

    private void advanceLoop(TimeSimulator instance, boolean callAdvanceBy, int delay) {
        if (callAdvanceBy) {
            instance.advanceTimeBy(delay);
        }
        else {
            instance.advanceTimeTo(instance.getCurrentTimestamp() + delay);
        }
    }

    @Test
    public void testForErrors() {
        expectThrows(IllegalArgumentException.class,
            () -> {
                TimeSimulator instance = new TimeSimulator();
                instance.advanceTimeBy(-1);
            });
        expectThrows(IllegalArgumentException.class,
            () -> {
                TimeSimulator instance = new TimeSimulator();
                instance.advanceTimeTo(-1);
            });
        expectThrows(IllegalArgumentException.class,
            () -> {
                TimeSimulator instance = new TimeSimulator();
                instance.postCallback(null);
            });
        expectThrows(IllegalArgumentException.class,
            () -> {
                TimeSimulator instance = new TimeSimulator();
                instance.setTimeout(null, 0);
            });
        expectThrows(IllegalArgumentException.class,
            () -> {
                TimeSimulator instance = new TimeSimulator();
                instance.setTimeout(() -> {}, -1);
            });
    }
    
    @Test(dataProvider = "createTestAdvanceData")
    public void testAdvance(boolean callAdvanceBy) {
        TimeSimulator instance = new TimeSimulator();
        assertEquals(instance.getCurrentTimestamp(), 0);

        List<String> callbackLogs = new ArrayList<String>();

        advanceLoop(instance, callAdvanceBy, 10);
        assertEquals(instance.getCurrentTimestamp(), 10);
        assertThat(callbackLogs, is(Arrays.asList()));

        instance.postCallback(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":cac4e224-15b6-45af-8df4-0a4d43b2ae05"));
        instance.postCallback(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":757d903d-376f-4e5f-accf-371fd5f06c3d"));
        instance.postCallback(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":245bd145-a538-49b8-b7c8-733f77e5d245"));

        advanceLoop(instance, callAdvanceBy, 0);
        assertEquals(instance.getCurrentTimestamp(), 10);
        assertThat(callbackLogs, is(Arrays.asList(
            "10:cac4e224-15b6-45af-8df4-0a4d43b2ae05",
            "10:757d903d-376f-4e5f-accf-371fd5f06c3d",
            "10:245bd145-a538-49b8-b7c8-733f77e5d245")));

        callbackLogs.clear();
        advanceLoop(instance, callAdvanceBy, 0);
        assertEquals(instance.getCurrentTimestamp(), 10);
        assertThat(callbackLogs, is(Arrays.asList()));

        instance.setTimeout(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":3978252e-188f-4f03-96e2-8036f13dfae2"), 5);
        instance.setTimeout(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":e1e039a0-c83a-43da-8f29-81725eb7147f"), 6);
        Object testTimeoutId = instance.setTimeout(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":ebf9dd1d-7157-420a-ac16-00a3fde9bf4e"), 11);
        assertNotNull(testTimeoutId);

        advanceLoop(instance, callAdvanceBy, 4);
        assertEquals(instance.getCurrentTimestamp(), 14);
        assertThat(callbackLogs, is(Arrays.asList()));

        advanceLoop(instance, callAdvanceBy, 1);
        assertEquals(instance.getCurrentTimestamp(), 15);
        assertThat(callbackLogs, is(Arrays.asList(
            "15:3978252e-188f-4f03-96e2-8036f13dfae2")));

        callbackLogs.clear();
        advanceLoop(instance, callAdvanceBy, 1);
        assertEquals(16, instance.getCurrentTimestamp());
        assertThat(callbackLogs, is(Arrays.asList(
            "16:e1e039a0-c83a-43da-8f29-81725eb7147f")));

        callbackLogs.clear();
        advanceLoop(instance, callAdvanceBy, 4);
        assertEquals(instance.getCurrentTimestamp(), 20);
        assertThat(callbackLogs, is(Arrays.asList()));

        assertEquals(instance.getPendingEventCount(), 1);
        instance.clearTimeout(testTimeoutId);        
        assertEquals(instance.getPendingEventCount(), 0);
        // test repeated cancellation of same id doesn't cause problems.
        instance.clearTimeout(testTimeoutId);
        assertEquals(instance.getPendingEventCount(), 0);

        instance.postCallback(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":6d3a5586-b81d-4ca5-880b-2b711881a14e"));
        testTimeoutId = instance.setTimeout(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":8722d9a6-a7d4-47fe-a6d4-eee624fb0740"), 3);
        assertNotNull(testTimeoutId);
        instance.setTimeout(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":2f7deeb1-f857-4f29-82de-b4168133f093"), 4);
        Object testTimeoutId2 = instance.setTimeout(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":42989f22-a6d1-48ff-a554-86f79e87321e"), 3);
        assertNotNull(testTimeoutId2);
        instance.setTimeout(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":9b463fec-6a9c-44cc-8165-e106080b18fc"), 0);
        Object testTimeoutId3 = instance.postCallback(() ->
            callbackLogs.add(instance.getCurrentTimestamp() +  ":56805433-1f02-4327-b190-50862c0ba93e"));
        assertNotNull(testTimeoutId3);

        assertThat(callbackLogs, is(Arrays.asList()));

        advanceLoop(instance, callAdvanceBy, 2);
        assertEquals(instance.getCurrentTimestamp(), 22);
        assertThat(callbackLogs, is(Arrays.asList(
            "20:6d3a5586-b81d-4ca5-880b-2b711881a14e",
            "20:9b463fec-6a9c-44cc-8165-e106080b18fc",
            "20:56805433-1f02-4327-b190-50862c0ba93e")));

        callbackLogs.clear();
        instance.clearTimeout(testTimeoutId);
        advanceLoop(instance, callAdvanceBy, 3);
        assertEquals(instance.getCurrentTimestamp(), 25);
        assertThat(callbackLogs, is(Arrays.asList(
            "23:42989f22-a6d1-48ff-a554-86f79e87321e",
            "24:2f7deeb1-f857-4f29-82de-b4168133f093")));

        callbackLogs.clear();
        instance.clearTimeout(testTimeoutId);

        instance.postCallback(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":6d3a5586-b81d-4ca5-880b-2b711881a14e"));
        instance.setTimeout(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":8722d9a6-a7d4-47fe-a6d4-eee624fb0740"), 3);
        instance.setTimeout(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":2f7deeb1-f857-4f29-82de-b4168133f093"), 4);
        testTimeoutId = instance.setTimeout(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":42989f22-a6d1-48ff-a554-86f79e87321e"), 3);
        assertNotNull(testTimeoutId);

        instance.setTimeout(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":9b463fec-6a9c-44cc-8165-e106080b18fc"), 0);
        instance.postCallback(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":56805433-1f02-4327-b190-50862c0ba93e"));
        testTimeoutId3 = instance.postCallback(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":5f08ae56-f596-4703-a9ab-3a66c6c29c07"));
        assertThat(callbackLogs, is(Arrays.asList()));

        assertEquals(instance.getPendingEventCount(), 7);
        instance.clearTimeout(testTimeoutId3);
        assertEquals(instance.getPendingEventCount(), 6);

        advanceLoop(instance, callAdvanceBy, 5);
        assertEquals(instance.getCurrentTimestamp(), 30);
        assertThat(callbackLogs, is(Arrays.asList(
            "25:6d3a5586-b81d-4ca5-880b-2b711881a14e",
            "25:9b463fec-6a9c-44cc-8165-e106080b18fc",
            "25:56805433-1f02-4327-b190-50862c0ba93e",
            "28:8722d9a6-a7d4-47fe-a6d4-eee624fb0740",
            "28:42989f22-a6d1-48ff-a554-86f79e87321e",
            "29:2f7deeb1-f857-4f29-82de-b4168133f093")));

        callbackLogs.clear();
        instance.clearTimeout(testTimeoutId3); // test already used timeout cancellation isn't a problem.
        instance.clearTimeout(testTimeoutId); // test already used timeout cancellation isn't a problem.
        instance.clearTimeout(testTimeoutId2);  // test already used timeout isn't a problem.
        instance.clearTimeout(null);  // test unexpected doesn't cause problems.
        instance.clearTimeout(null);
        instance.clearTimeout("jal");  // test unexpected doesn't cause problems.
        instance.clearTimeout(3);

        advanceLoop(instance, callAdvanceBy, 5);
        assertEquals(instance.getCurrentTimestamp(), 35);
        assertThat(callbackLogs, is(Arrays.asList()));

        assertEquals(instance.getPendingEventCount(), 0);
    }

    @DataProvider
    public Object[][] createTestAdvanceData() {
        return new Object[][]{
            { true },
            { false }
        };
    }

    @Test
    public void testNestedCallbackPosts() {
        TimeSimulator instance = new TimeSimulator();

        assertEquals(instance.getCurrentTimestamp(), 0);

        List<String> callbackLogs = new ArrayList<>();

        instance.postCallback(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":cac4e224-15b6-45af-8df4-0a4d43b2ae05"));
        instance.postCallback(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":757d903d-376f-4e5f-accf-371fd5f06c3d"));
        instance.postCallback(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":245bd145-a538-49b8-b7c8-733f77e5d245"));

        instance.advanceTimeBy(0);
        assertEquals(instance.getCurrentTimestamp(), 0);
        assertThat(callbackLogs, is(Arrays.asList(
            "0:cac4e224-15b6-45af-8df4-0a4d43b2ae05",
            "0:757d903d-376f-4e5f-accf-371fd5f06c3d",
            "0:245bd145-a538-49b8-b7c8-733f77e5d245")));

        callbackLogs.clear();
        instance.advanceTimeBy(0);
        assertEquals(instance.getCurrentTimestamp(), 0);
        assertThat(callbackLogs, is(Arrays.asList()));

        instance.setTimeout(() ->
        {
            callbackLogs.add(instance.getCurrentTimestamp() + ":3978252e-188f-4f03-96e2-8036f13dfae2");
            instance.setTimeout(() ->
                callbackLogs.add(instance.getCurrentTimestamp() + ":240fbcc0-9930-4e96-9b62-356458ee0a9f"), 4);
        }, 5);

        instance.setTimeout(() ->
            callbackLogs.add(instance.getCurrentTimestamp() + ":e1e039a0-c83a-43da-8f29-81725eb7147f"), 6);

        instance.advanceTimeTo(14);
        assertEquals(instance.getCurrentTimestamp(), 14);
        assertEquals(instance.getPendingEventCount(), 0);
        assertThat(callbackLogs, is(Arrays.asList(
            "5:3978252e-188f-4f03-96e2-8036f13dfae2",
            "6:e1e039a0-c83a-43da-8f29-81725eb7147f",
            "9:240fbcc0-9930-4e96-9b62-356458ee0a9f")));
        
        callbackLogs.clear();
        instance.advanceTimeTo(4); // test backward movement of time.
        assertEquals(instance.getCurrentTimestamp(), 4);
        assertEquals(instance.getPendingEventCount(), 0);
        assertThat(callbackLogs, is(Arrays.asList()));
    }

    @Test
    public void testPerformanceForOverOneThousand() {
        TimeSimulator instance = new TimeSimulator();

        int timeLimit = 10_000;
        for (int i = 0; i < timeLimit; i++) {
            instance.setTimeout(() -> { }, i);
        }
        instance.advanceTimeTo(timeLimit);
    }
}
