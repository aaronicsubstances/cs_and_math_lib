package com.aaronicsubstances.cs_and_math.sorting;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

public class ExternalSortTest {
    private final Random randGen = new Random();

    @Test(dataProvider = "createTestSortData")
    public void testSort(int inputSize) throws Exception { 
        // arrange
        Comparator<Integer> sortFunc = (a, b) -> Integer.compare(a, b);
        SortConfiguration sortConfig = new SortConfiguration();
        sortConfig.setMaximumRamUsage(50);
        sortConfig.setMinimumChunkRamUsage(10);
        List<String> actualLogs = new ArrayList<>();
        TestStorage storage = new TestStorage(actualLogs);

        List<Integer> input = new ArrayList<>();
        for (int i = 0; i < inputSize; i++) {
            input.add(randGen.nextInt());
        }
        List<String> expectedLogs = generateExpectedLogs(input.size(), sortConfig);
        /*System.out.format("Expected logs for input size %d:%n", input.size());
        for (String log : expectedLogs) {
            System.out.println(log);
        }
        System.out.println();*/

        // act
        CloseableIterator<Integer> result = ExternalSort.sort(
            input.iterator(), sortFunc, sortConfig, storage);
        List<Integer> actual = SortingUtils.iteratorToList(result);
        result.close();

        // assert
        List<Integer> expected = new ArrayList<>(input);
        expected.sort(sortFunc);
        assertThat(actual, is(expected));
        assertThat(actualLogs, is(expectedLogs));
        assertEquals(storage.getBucketCount(), 0);
    }

    @DataProvider
    public Object[][] createTestSortData() {
        return new Object[][]{
            { 0 },
            { 1 },
            { 2 },
            { 4 },
            { 16 },
            { 32 }, 
            { 49 },
            { 50 }, 
            { 51 },
            { 64 },
            { 256 },
            { 500 }, 
            { 512 },
            { 1000 },
            { 10000 },
        };
    }

    private List<String> generateExpectedLogs(int inputSize, SortConfiguration sortConfig) {
        List<String> expectedLogs = new ArrayList<>();
        int sortedChunkCount = (int)Math.ceil(1.0 * inputSize / sortConfig.getMaximumRamUsage());
        if (sortedChunkCount > 1) {
            int bucketId = 1;
            List<Integer> chunkGroupSizes = new ArrayList<>();
            // set expected logs for initial files.
            for (int i = 0; i < sortedChunkCount; i++) {
                int sz = sortConfig.getMaximumRamUsage();
                // treat last chunk differently 
                if (i == sortedChunkCount - 1) {
                    sz = inputSize - (i * sortConfig.getMaximumRamUsage());
                }
                expectedLogs.add(String.format("%s.created", bucketId));
                expectedLogs.add(String.format("%s.written=%d", bucketId, 
                    sz));
                chunkGroupSizes.add(sz);
                bucketId++;
            }
            
            // set expected logs for intermediate files.
            int chunkGroupCount = sortConfig.getChunkGroupCount();
            sortedChunkCount = (int)Math.ceil(1.0 * sortedChunkCount / chunkGroupCount);
            while (sortedChunkCount > 1) {
                List<Integer> nextChunkGroupSizes = new ArrayList<>();
                for (int i = 0; i < sortedChunkCount; i++) {
                    int sz = 0;
                    for (int j = i * chunkGroupCount; j < (i + 1) * chunkGroupCount
                            && j < chunkGroupSizes.size(); j++) {
                        sz += chunkGroupSizes.get(j);
                    }
                    expectedLogs.add(String.format("%s.created", bucketId));
                    expectedLogs.add(String.format("%s.written=%d", bucketId, 
                        sz));
                    nextChunkGroupSizes.add(sz);
                    bucketId++;
                }
                sortedChunkCount = (int)Math.ceil(1.0 * sortedChunkCount / chunkGroupCount);
                chunkGroupSizes = nextChunkGroupSizes;
            }

            // set expected logs for final output file
            expectedLogs.add(String.format("%s.created", bucketId));
            expectedLogs.add(String.format("%s.written=%d", bucketId, 
                inputSize));
        }
        return expectedLogs;
    }
}