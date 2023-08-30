package com.aaronicsubstances.cs_and_math;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MicroOrmHelpers {

    public static TupleItemAllocator createTupleItemAllocator(
            int tupleLength,
            TupleIntrospector tupleIntrospector) {
        Objects.requireNonNull(tupleIntrospector,
            "tupleIntrospector");
        TupleItemAllocator instance = new TupleItemAllocator(
            tupleLength, tupleIntrospector);
        return instance;
    }

    public static class TupleItemAllocator {
        private final TupleIntrospector tupleIntrospector;
        private final int tupleLength;
        private final Map<String, Integer> state = new HashMap<>();

        private TupleItemAllocator(int tupleLength,
                TupleIntrospector tupleIntrospector) {
            this.tupleLength = tupleLength;
            this.tupleIntrospector = tupleIntrospector;
        }

        public int allocate(String name) {
            int startIdx = 0;
            if (state.containsKey(name)) {
                startIdx = state.get(name) + 1;
            }
            for (int i = startIdx; i < tupleLength; i++) {
                if (tupleIntrospector.hasPropAt(i, name)) {
                    state.put(name, i);
                    return i;
                }
            }
            return -1;
        }

        public void reset() {
            state.clear();
        }
    }

    @FunctionalInterface
    public static interface TupleIntrospector {
        boolean hasPropAt(int index, String name);    
    }
}
