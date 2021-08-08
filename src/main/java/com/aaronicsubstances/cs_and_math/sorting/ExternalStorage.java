package com.aaronicsubstances.cs_and_math.sorting;

public interface ExternalStorage {
    String createBucket();
    void deleteBucket(String bucketId);
    Object openStream(String bucketId,
        boolean openForWriting, boolean truncateBeforeWriting,
        int bufferSize);
    void closeStream(Object stream);
    Object deserializeFrom(Object stream, Class<?> classOfItem);
    void serializeTo(Object stream, Object item);
    int estimateSerializedSize(Object item);
}