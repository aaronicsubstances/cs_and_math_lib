package com.aaronicsubstances.cs_and_math.sorting;

import java.util.Iterator;

public class ExternalSortResult<T> implements Iterator<T> {
    private final String bucketId;
    private final ExternalStorage storage;
    private final Object stream;
    private T currentItem;

    public ExternalSortResult(String bucketId, ExternalStorage storage) {
        this.bucketId = bucketId;
        this.storage = storage;
        this.stream = storage.openStream(bucketId, false, false);
        advanceStream();
    }

    @SuppressWarnings("unchecked")
    private void advanceStream() {
        currentItem = (T)storage.deserializeFrom(stream);
        if (currentItem == null) {
            storage.closeStream(stream);
            storage.deleteBucket(bucketId);
        }
    }

    @Override
    public boolean hasNext() {
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