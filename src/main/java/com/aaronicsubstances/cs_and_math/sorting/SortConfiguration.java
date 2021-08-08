package com.aaronicsubstances.cs_and_math.sorting;

import java.util.ArrayList;
import java.util.List;

public class SortConfiguration {
    private int maximumRamUsage;
    private int minimumChunkRamUsage;
    private Class<?> classOfItem;

    public SortConfiguration() {
    }

    public SortConfiguration(int maximumRamUsage, int minimumChunkRamUsage) {
        this.maximumRamUsage = maximumRamUsage;
        this.minimumChunkRamUsage = minimumChunkRamUsage;
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

    public Class<?> getClassOfItem() {
        return classOfItem;
    }

    public void setClassOfItem(Class<?> classOfItem) {
        this.classOfItem = classOfItem;
    }

    public int getChunkGroupCount() {
        int chunkGroupCount = 0;
        if (minimumChunkRamUsage != 0) {
            chunkGroupCount = maximumRamUsage / minimumChunkRamUsage;
        }
        // must be at least 2
        return Math.max(2, chunkGroupCount);
    }
}