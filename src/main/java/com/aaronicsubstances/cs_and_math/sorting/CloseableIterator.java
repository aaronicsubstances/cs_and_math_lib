package com.aaronicsubstances.cs_and_math.sorting;

import java.util.Iterator;

public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable {
}