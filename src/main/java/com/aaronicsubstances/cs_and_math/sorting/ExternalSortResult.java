package com.aaronicsubstances.cs_and_math.sorting;

import java.util.Iterator;

public class ExternalSortResult<T> implements CloseableIterator<T> {
    private final String bucketId;
    private final ExternalStorage storage;
    private final int bufferSize;

    private Object stream;
    private T currentItem;

    public ExternalSortResult(String bucketId, ExternalStorage storage,
            int bufferSize) {
        this.bucketId = bucketId;
        this.storage = storage;
        this.bufferSize = bufferSize;
    }

    @Override
    public void close() {
        if (stream != null) {
            storage.closeStream(stream);
        }
        storage.deleteBucket(bucketId);
    }

    @SuppressWarnings("unchecked")
    private void advanceStream() {
        currentItem = (T)storage.deserializeFrom(stream);
    }

    @Override
    public boolean hasNext() {
        if (stream == null) {
            stream = storage.openStream(bucketId, false, false, bufferSize);
            advanceStream();
        }
        return currentItem != null;
    }

    @Override
    public T next() {
        T nextItem = currentItem;
        advanceStream();
        return nextItem;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}