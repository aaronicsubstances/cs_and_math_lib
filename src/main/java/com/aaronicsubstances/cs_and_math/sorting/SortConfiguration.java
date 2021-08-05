package com.aaronicsubstances.cs_and_math.sorting;

import java.util.ArrayList;
import java.util.List;

public class SortConfiguration {
    private int maximumRamUsage; // 100MB
    private int minimumChunkRamUsage; // 4MB

    public SortConfiguration() {
    }

    public int getMaximumRamUsage() {
        return maximumRamUsage;
    }

    public void setMaximumRamUsage(int maximumRamUsage) {
        this.maximumRamUsage = maximumRamUsage;
    }

    public int getMinimumChunkRamUsage() {
        return minimumChunkRamUsage;
    }

    public void setMinimumChunkRamUsage(int minimumChunkRamUsage) {
        this.minimumChunkRamUsage = minimumChunkRamUsage;
    }

    public int calculateChunkGroupSize(int sortedChunkCount) {
        // Assuming maximumRamUsage = 100MB, minimumChunkRamUsage = 4MB,
        // then maximumChunkGroupSize = 100/4 = 25
        //
        // e.g. 1: if totalDataSize = 900MB, then
        // sortedChunkCount = 900/100 = 9
        // chunkGroupSizes = [ 9 ]
        //
        // e.g. 2: if totalDataSize = 50_000 MB, then
        // sortedChunkCount = 50_000/100 = 500
        // chunkGroupSizes = [ 25, 20 ]

        int maximumChunkGroupSize = maximumRamUsage / minimumChunkRamUsage;
        return Math.min(sortedChunkCount, maximumChunkGroupSize);
    }
}