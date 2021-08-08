package com.aaronicsubstances.cs_and_math.sorting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestStorage implements ExternalStorage {
    private final Map<String, List<Object>> buckets;
    private final List<String> logs;
    private int autoInc;

    public TestStorage(List<String> logs) {
        this.buckets = new HashMap<>();
        this.logs = logs;
    }

    @Override
    public String createBucket() {
        String bucketId = "" + (++autoInc);
        List<Object> list = new ArrayList<>();
        list.add(0);
        buckets.put(bucketId, list);
        logs.add(String.format("%s.created", bucketId));
        return bucketId;
    }

    @Override
    public void deleteBucket(String bucketId) {
        buckets.remove(bucketId);
    }

    @Override
    public Object openStream(String bucketId,
            boolean openForWriting, boolean truncateBeforeWriting,
            int bufferSize) {                
        List<Object> list = buckets.get(bucketId);
        if (openForWriting) {
            if (truncateBeforeWriting) {
                list.clear();
                list.add(-1);
            }
            else {
                list.set(0, -1);
            }
        }
        else {
            list.set(0, 0);
        }
        return bucketId;
    }

    @Override
    public void closeStream(Object stream) {
        String bucketId = (String)stream;
        List<Object> list = buckets.get(bucketId);
        if (list.get(0).equals(-1)) {
            // means stream was opened for writing.
            logs.add(String.format("%s.written=%d", bucketId, list.size() - 1));
        }
    }

    @Override
    public Object deserializeFrom(Object stream, Class<?> classOfItem) {
        String bucketId = (String)stream;
        List<Object> list = buckets.get(bucketId);
        int deserializedCount = (int)list.get(0);
        if (deserializedCount == list.size() - 1) {
            return null;
        }
        Object next = list.get(deserializedCount + 1);
        list.set(0, deserializedCount + 1);
        return next;
    }
    
    @Override
    public void serializeTo(Object stream, Object item) {
        String bucketId = (String)stream;
        List<Object> list = buckets.get(bucketId);
        list.add(item);
    }

    @Override
    public int estimateSerializedSize(Object item) {
        return 1;
    }

    public int getBucketCount() {
        return buckets.size();
    }
}