package com.aaronicsubstances.cs_and_math.sorting;

import java.util.Iterator;

public class CloseableIteratorAdapter<T> implements CloseableIterator<T> {
    private final Iterator<T> wrapped;

    public CloseableIteratorAdapter(Iterator<T> wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean hasNext() {
        return wrapped.hasNext();
    }

    @Override
    public T next() {
        return wrapped.next();
    }

    @Override
    public void remove() {
        wrapped.remove();
    }
}