package com.aaronicsubstances.cs_and_math.sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Based on https://en.wikipedia.org/wiki/External_sorting
 */
public class ExternalSort {

    public static <T> Iterator<T> sort(Iterator<T> data, Comparator<T> sortFunc,
            SortConfiguration sortConfiguration, ExternalStorage storage) {
                
        // phase 1: split iterator into chunks and sort each chunk
        CreateSortedChunksRetResult<T> splitResult = createSortedChunks(data, sortFunc,
            sortConfiguration, storage);
        if (splitResult.finalSortResult != null) {
            return splitResult.finalSortResult;
        }

        // phase 2: perform multiple passes of multiway merge algorithm
        List<String> sortedChunkIds = splitResult.sortedChunkIds;
        while (sortedChunkIds.size() > 1) {
            int chunkGroupSize = sortConfiguration.calculateChunkGroupSize(
                sortedChunkIds.size());
            List<String> outputChunkIds = new ArrayList<>();
            for (int i = 0; i < sortedChunkIds.size(); i += chunkGroupSize) {
                int startIdx = i;
                int endIdx = Math.min(i + chunkGroupSize, sortedChunkIds.size());
                List<String> subsetOfSortedChunkIds = sortedChunkIds.subList(startIdx, endIdx);
                String outputChunkId = performMultiWayMerge(subsetOfSortedChunkIds,
                    sortFunc, sortConfiguration, storage);
                outputChunkIds.add(outputChunkId);
            }
            sortedChunkIds = outputChunkIds;
        }

        // phase 3: generate iterator from final sorted chunk.
        if (sortedChunkIds.isEmpty()) {
            return Collections.emptyIterator();
        }

        return new ExternalSortResult<T>(sortedChunkIds.get(0), storage);
    }

    /**
     * 1. Read 100 MB of the data in main memory and sort by some conventional method, 
     *    like quicksort.
     * 2. Write the sorted data to disk.
     * 3. Repeat steps 1 and 2 until all of the data is in sorted 100 MB chunks 
     *    (there are 900MB / 100MB = 9 chunks), which now need to be merged into one single output file.
     */
    static <T> CreateSortedChunksRetResult<T> createSortedChunks(Iterator<T> data, 
            Comparator<T> sortFunc, SortConfiguration sortConfiguration,
            ExternalStorage storage) {        
        List<String> sortedChunkIds = new ArrayList<>();
        List<T> sortedList = new ArrayList<>();
        int currentChunkSize = 0;

        while (data.hasNext()) {
            T item = data.next();
            int serializedSize = storage.estimateSerializedSize(item);
            // At least one chunk must be saved, 
            // regardless of maximum RAM usage setting.
            if (currentChunkSize >= sortConfiguration.getMaximumRamUsage()) {
                sortedList.sort((a, b)->{
                    return sortFunc.compare(a, b);
                });
                String chunkId = storage.createBucket();
                saveSortedChunks(chunkId, sortedList, storage);
                sortedChunkIds.add(chunkId);

                sortedList.clear();
                currentChunkSize = 0;
            }
            sortedList.add(item);
            currentChunkSize += serializedSize;
        }

        // sort remaining items.
        sortedList.sort((a, b)->{
            return sortFunc.compare(a, b);
        });

        // perform optimization of avoiding external storage
        // completely, if we have not touched it up until
        // this stage.
        if (sortedChunkIds.isEmpty()) {
            Iterator<T> finalSortResult = sortedList.iterator(); 
            return new CreateSortedChunksRetResult<T>(null, finalSortResult);
        }

        // save remaining items.
        String chunkId = storage.createBucket();
        saveSortedChunks(chunkId, sortedList, storage);
        sortedChunkIds.add(chunkId);
        return new CreateSortedChunksRetResult<T>(sortedChunkIds, null);
    }

    static <T> String performMultiWayMerge(List<String> sortedChunkIds, Comparator<T> sortFunc,
            SortConfiguration sortConfiguration, ExternalStorage storage) {
        String outputChunkId = storage.createBucket();
        Object outputStream = null;
        try {
            outputStream = storage.openStream(outputChunkId, true, true);
            List<Iterator<T>> sortedChunkIterators = new ArrayList<>();
            for (String sortedChunkId : sortedChunkIds) {
                Iterator<T> iterator = new ExternalSortResult<>(sortedChunkId, storage);
                sortedChunkIterators.add(iterator);
            }
        }
        finally {
            if (outputStream != null) {
                storage.closeStream(outputChunkId);
            }
        }
        return outputChunkId;
    }

    private static <T> void saveSortedChunks(String bucketId,
            List<T> sortedList, ExternalStorage storage) {
        Object chunkStream = null;
        try {
            chunkStream = storage.openStream(bucketId, true, true);
            for (T item: sortedList) {
                storage.serializeTo(chunkStream, item);
            }
        }
        finally {
            if (chunkStream != null) {
                storage.closeStream(chunkStream);
            }
        }
    }

    static class CreateSortedChunksRetResult<T> {
        public List<String> sortedChunkIds;
        public Iterator<T> finalSortResult;

        public CreateSortedChunksRetResult(List<String> sortedChunkIds, Iterator<T> finalSortResult) {
            this.sortedChunkIds = sortedChunkIds;
            this.finalSortResult = finalSortResult;
        }
    }
}
