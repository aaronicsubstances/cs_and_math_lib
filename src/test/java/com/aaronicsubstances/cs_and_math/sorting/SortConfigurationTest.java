package com.aaronicsubstances.cs_and_math.sorting;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SortConfigurationTest {

    @Test(dataProvider = "createTestGetChunkGroupCountData")
    public void testGetChunkGroupCount(int maximumRamUsage, int minimumChunkRamUsage, int expected) {
        SortConfiguration instance = new SortConfiguration(maximumRamUsage, minimumChunkRamUsage);
        int actual = instance.getChunkGroupCount();
        assertEquals(actual, expected);
    }

    @DataProvider
    public Object[][] createTestGetChunkGroupCountData() {
        return new Object[][]{
            { 0, 0, 2 },
            { 0, 1, 2 },
            { 8, 5, 2 },
            { 18, 5, 3 },
            { 80, 10, 8 },
            { 78, 10, 7 },
            { 100, 2, 50 }
        };
    }
}