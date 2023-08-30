package com.aaronicsubstances.cs_and_math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import static com.aaronicsubstances.cs_and_math.MicroOrmHelpers.*;

public class MicroOrmHelpersTest {

    /*@Test
    public void testCreateTupleItemAllocator1() {
        int tupleLength = 0;
        TupleIntrospector tupleIntrospector = (index, name) -> {
            return false;
        };
        TupleItemAllocator instance = createTupleItemAllocator(
            tupleLength, tupleIntrospector);
        int expected = -1;
        int actual = instance.allocate("ab");
        assertEquals(actual, expected);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testCreateTupleItemAllocator2() {
        TupleItemAllocator instance = createTupleItemAllocator(
            1, null);
    }

    public void testCreateTupleItemAllocator3() {
        TupleIntrospector tupleIntrospector = (index, name) -> {
            throw new UnsupportedOperationException("should not be called");
        };
        TupleItemAllocator instance = createTupleItemAllocator(
            -1, tupleIntrospector);
        int expected = -1;
        int actual = instance.allocate("ab");
        assertEquals(actual, expected);
    }

    @Test
    public void testCreateTupleItemAllocator4() {
        Set<String> tupleItem1 = new HashSet<>(
            Arrays.asList("name", "value"));
        Set<String> tupleItem2 = new HashSet<>(
            Arrays.asList("text", "age", "location"));
        Set<String> tupleItem3 = new HashSet<>(
            Arrays.asList("name", "age", "status"));
        List<Set<String>> tupleDescriptor = Arrays.asList(
            tupleItem1, tupleItem2, tupleItem3);
        TupleIntrospector tupleIntrospector = (index, name) -> {
            return tupleDescriptor.get(index).contains(name);
        };
        TupleItemAllocator instance = createTupleItemAllocator(
            tupleDescriptor.size(), tupleIntrospector);
        int expected = -1;
        int actual = instance.allocate("ab");
        assertEquals(actual, expected);

        expected = 0;
        actual = instance.allocate("name");
        assertEquals(actual, expected);

        expected = 2;
        actual = instance.allocate("name");
        assertEquals(actual, expected);

        expected = -1;
        actual = instance.allocate("name");
        assertEquals(actual, expected);

        instance.reset();

        expected = 0;
        actual = instance.allocate("name");
        assertEquals(actual, expected);

        expected = 1;
        actual = instance.allocate("text");
        assertEquals(actual, expected);

        expected = 2;
        actual = instance.allocate("status");
        assertEquals(actual, expected);

        expected = 1;
        actual = instance.allocate("age");
        assertEquals(actual, expected);

        expected = 2;
        actual = instance.allocate("age");
        assertEquals(actual, expected);

        expected = -1;
        actual = instance.allocate("age");
        assertEquals(actual, expected);

        expected = -1;
        actual = instance.allocate("text");
        assertEquals(actual, expected);

        expected = 1;
        actual = instance.allocate("location");
        assertEquals(actual, expected);

        expected = -1;
        actual = instance.allocate("text");
        assertEquals(actual, expected);
    }*/
}