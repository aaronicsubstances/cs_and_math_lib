package com.aaronicsubstances.cs_and_math.sorting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SortingUtils {
    
    public static <T> List<T> iteratorToList(Iterator<T> iterator) {
        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }
}