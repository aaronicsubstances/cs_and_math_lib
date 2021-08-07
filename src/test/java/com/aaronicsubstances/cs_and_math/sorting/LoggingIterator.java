package com.aaronicsubstances.cs_and_math.sorting;

import java.util.Iterator;
import java.util.List;

public class LoggingIterator<T> implements Iterator<T> {
    private final String name;
    private final Iterator<T> wrapped;
    private final List<String> logs;

    public LoggingIterator(String name, Iterator<T> wrapped, List<String> logs) {
        this.name = name;
        this.wrapped = wrapped;
        this.logs = logs;
    }

    @Override
    public boolean hasNext() {
        boolean hasMore = wrapped.hasNext();
        if (!hasMore) {
            logs.add(String.format("%s:end", name));
        }
        return hasMore;
    }

    @Override
    public T next() {
        T nextItem = wrapped.next();
        logs.add(String.format("%s:next:%s", name, nextItem));
        return nextItem;
    }

    @Override
    public void remove() {
        wrapped.remove();
    }
}
