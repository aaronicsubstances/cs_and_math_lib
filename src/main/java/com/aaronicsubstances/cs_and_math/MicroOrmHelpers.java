package com.aaronicsubstances.cs_and_math;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MicroOrmHelpers {

    public static TupleItemAllocator createTupleItemAllocator(
            int tupleLength, int[] itemSizes,
            TupleIntrospector tupleIntrospector) {
        Objects.requireNonNull(itemSizes,
            "itemSizes");
        Objects.requireNonNull(tupleIntrospector,
            "tupleIntrospector");
        TupleItemAllocator instance = new TupleItemAllocator(
            tupleLength, itemSizes, tupleIntrospector);
        return instance;
    }

    public static class TupleItemAllocator {
        private final TupleIntrospector tupleIntrospector;
        private final int tupleLength;
        private final int[] itemSizes;
        private boolean doingFlexibleAllocation;
        private int tupleIndex;
        private boolean canProceedToNext;

        private TupleItemAllocator(
                int tupleLength, int[] itemSizes,
                TupleIntrospector tupleIntrospector) {
            this.tupleLength = tupleLength;
            this.itemSizes = itemSizes;
            this.tupleIntrospector = tupleIntrospector;
        }

        public int allocate(int index, String name) {
            // three cases
            // 1. 3 items, 2 items, ...,
            //    - in this case just zoom into wherever
            //      index falls, and use it. 
            // if ... is just one,
            //    - in this case just use last one.
            // if ... is more than one.
            //    - in this case use state.
            if (!doingFlexibleAllocation) {
                int sum = 0;
                for (int i = 0; i < itemSizes.length; i++) {
                    int itemSize = itemSizes[i];
                    if (sum + itemSize < index) {
                        sum += itemSize;
                    }
                    else {
                        return tupleIntrospector.hasPropAt(
                            i, name) ? i : -1;
                    }
                }
                if (tupleLength - itemSizes.length < 2) {
                    return tupleIntrospector.hasPropAt(
                        itemSizes.length, name) ?
                            itemSizes.length : -1;
                }
                doingFlexibleAllocation = true;
                tupleIndex = itemSizes.length;
                canProceedToNext = false;
            }
            return allocateFlexible(name);
        }

        private int allocateFlexible(String name) {
            for ( ; tupleIndex < tupleLength; tupleIndex++) {
                if (tupleIntrospector.hasPropAt(tupleIndex, name)) {
                    canProceedToNext = true;
                    return tupleIndex;
                }
                canProceedToNext = !canProceedToNext;
                if (canProceedToNext) {
                    // meaning old value was false.
                    break;
                }
            }
            return -1;
        }

        public void reset() {
            doingFlexibleAllocation = false;
        }
    }

    @FunctionalInterface
    public static interface TupleIntrospector {
        boolean hasPropAt(int index, String name);    
    }
}
