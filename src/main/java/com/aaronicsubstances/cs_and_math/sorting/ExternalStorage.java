package com.aaronicsubstances.cs_and_math.sorting;

public interface ExternalStorage {
    String createBucket();
    void deleteBucket(String bucketId);
    Object openStream(String bucketId,
        boolean openForWriting, boolean truncateBeforeWriting);
    void closeStream(Object stream);
    byte[] deserializeFrom(Object stream);
    void serializeTo(Object stream, Object item);
    int estimateSerializedSize(Object item);
}