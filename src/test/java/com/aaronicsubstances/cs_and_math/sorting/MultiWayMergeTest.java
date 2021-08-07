package com.aaronicsubstances.cs_and_math.sorting;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

public class MultiWayMergeTest {
    private final Comparator<Item> forwardSortFunc;
    private final Comparator<Item> reverseSortFunc;
    private final Comparator<Item> nullAcceptingSortFunc;

    public MultiWayMergeTest() {
        forwardSortFunc = (a, b) -> {
            if (a.score < b.score) {
                return -1;
            }
            else if (a.score > b.score) {
                return 1;
            }
            else {
                return 0;
            }
        };
        reverseSortFunc = (a, b) -> {
            return -1 * forwardSortFunc.compare(a, b);
        };
        nullAcceptingSortFunc = (a, b) -> {
            if (a == null || b == null) {
                // put nulls last.
                if (a == null && b == null) {
                    return 0;
                }
                else if (a == null) {
                    return 1;
                }
                else {
                    return -1;
                }
            }
            return forwardSortFunc.compare(a, b);
        };
    }

    @Test(dataProvider = "createTestMergeData")
    public void testMerge(List<List<Item>> itemLists,
            Comparator<Item> sortFunc, List<Item> expected) {
        List<Item> actual = SortingUtils.iteratorToList(MultiWayMerge.merge(itemLists.stream()
            .map(x -> x.iterator()).collect(Collectors.toList()), sortFunc));
        assertThat(actual, is(expected));      
    }

    @DataProvider
    public Object[][] createTestMergeData() {
        return new Object[][]{
            // test with empty list
            {
                Arrays.asList(),
                forwardSortFunc,
                Arrays.asList()
            },
            // test with lists which are not likely to be 
            // produced by external sort
            {
                Arrays.asList(
                    Arrays.asList(new Item("A", 1))
                ),
                forwardSortFunc,
                Arrays.asList(new Item("A", 1))
            },
            {
                Arrays.asList(
                    Arrays.asList(),
                    Arrays.asList(new Item("A", 1), new Item("B", 2)),
                    Arrays.asList()
                ),
                forwardSortFunc,
                Arrays.asList(new Item("A", 1), new Item("B", 2))
            },
            {
                Arrays.asList(
                    Arrays.asList(),
                    Arrays.asList(new Item("B", 2), new Item("A", 1)),
                    Arrays.asList()
                ),
                reverseSortFunc,
                Arrays.asList(new Item("B", 2), new Item("A", 1))
            },
            {
                Arrays.asList(
                    Arrays.asList(), Arrays.asList(), Arrays.asList()
                ),
                reverseSortFunc,
                Arrays.asList()
            },
            {
                Arrays.asList(
                    Arrays.asList(), Arrays.asList(),
                    Arrays.asList(), Arrays.asList()
                ),
                reverseSortFunc,
                Arrays.asList()
            },
            {
                Arrays.asList(
                    Arrays.asList(new Item("A", 10)),
                    Arrays.asList(new Item("B", 5)),
                    Arrays.asList(new Item("C", -1)),
                    Arrays.asList(new Item("D", 12))
                ),
                forwardSortFunc,
                Arrays.asList(new Item("C", -1), new Item("B", 5),
                    new Item("A", 10), new Item("D", 12))
            },
            {
                Arrays.asList(
                    Arrays.asList(new Item("A", 10)),
                    Arrays.asList(new Item("B", 5)),
                    Arrays.asList(new Item("C", -1)),
                    Arrays.asList(new Item("D", 12))
                ),
                reverseSortFunc,
                Arrays.asList(new Item("D", 12), new Item("A", 10),
                    new Item("B", 5), new Item("C", -1))
            },
            // test with lists of approximately equal sizes.
            {
                Arrays.asList(
                    Arrays.asList(new Item("A", 1), new Item("B", 7), new Item("C", 12)),
                    Arrays.asList(new Item("D", 3), new Item("E", 13), new Item("F", 15)),
                    Arrays.asList(new Item("G", 4), new Item("H", 14), new Item("I", 18)),
                    Arrays.asList(new Item("A", 2), new Item("B", 10), new Item("C", 17)),
                    Arrays.asList(new Item("D", 5), new Item("E", 9), new Item("F", 11)),
                    Arrays.asList(new Item("A", 6), new Item("B", 8), new Item("C", 16))
                ),
                forwardSortFunc,
                Arrays.asList(new Item("A", 1), new Item("A", 2), new Item("D", 3),
                    new Item("G", 4), new Item("D", 5), new Item("A", 6),
                    new Item("B", 7), new Item("B", 8), new Item("E", 9),
                    new Item("B", 10), new Item("F", 11), new Item("C", 12),
                    new Item("E", 13), new Item("H", 14), new Item("F", 15),
                    new Item("C", 16), new Item("C", 17), new Item("I", 18))
            },
            {
                Arrays.asList(
                    Arrays.asList(new Item("", 2), new Item("", 7), new Item("", 16)),
                    Arrays.asList(new Item("", 5), new Item("", 10), new Item("", 20)),
                    Arrays.asList(new Item("", 3), new Item("", 6), new Item("", 21)),
                    Arrays.asList(new Item("", 4), new Item("", 8), new Item("", 9))
                ),
                forwardSortFunc,
                Arrays.asList(new Item("", 2), new Item("", 3), new Item("", 4), new Item("", 5),
                    new Item("", 6), new Item("", 7), new Item("", 8), new Item("", 9), 
                    new Item("", 10), new Item("", 16), new Item("", 20), new Item("", 21))
            },
            {
                Arrays.asList(
                    Arrays.asList(new Item("", 16), new Item("", 7), new Item("", 2)),
                    Arrays.asList(new Item("", 20), new Item("", 10), new Item("", 5)),
                    Arrays.asList(new Item("", 21), new Item("", 6), new Item("", 3)),
                    Arrays.asList(new Item("", 9), new Item("", 8), new Item("", 4))
                ),
                reverseSortFunc,
                Arrays.asList(new Item("", 21), new Item("", 20), new Item("", 16), new Item("", 10),
                    new Item("", 9), new Item("", 8), new Item("", 7), new Item("", 6), 
                    new Item("", 5), new Item("", 4), new Item("", 3), new Item("", 2))
            },
            // test sort for stability
            {
                Arrays.asList(
                    Arrays.asList(new Item("X", 2), new Item("Y", 2)),
                    Arrays.asList(new Item("Z", 1), new Item("A", 2)),
                    Arrays.asList(new Item("B", 1), new Item("C", 1))
                ),
                forwardSortFunc,
                Arrays.asList(new Item("Z", 1), new Item("B", 1), new Item("C", 1), 
                    new Item("X", 2), new Item("Y", 2), new Item("A", 2))
            },
            {
                Arrays.asList(
                    Arrays.asList(new Item("X", 2), new Item("Y", 2)),
                    Arrays.asList(new Item("A", 2), new Item("Z", 1)),
                    Arrays.asList(new Item("B", 1), new Item("C", 1))
                ),
                reverseSortFunc,
                Arrays.asList(new Item("X", 2), new Item("Y", 2), new Item("A", 2),
                    new Item("Z", 1), new Item("B", 1), new Item("C", 1))
            },
            // test with comparator which accepts null
            {
                Arrays.asList(
                    Arrays.asList(new Item("age", 40), null, null),                    
                    Arrays.asList(new Item("e", 41)),
                    Arrays.asList()
                ),
                nullAcceptingSortFunc,
                Arrays.asList(new Item("age", 40), new Item("e", 41), null, null)
            }
        };
    }

    private static class Item {
        public String label;
        public int score;

        public Item(String label, int score) {
            this.label = label;
            this.score = score;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Item)) {
                return false;
            }
            Item other = (Item)o;
            if (label == null ? other.label != null : !label.equals(other.label)) {
                return false;
            }
            if (score != other.score) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public String toString() {
            return String.format("Item{label=%s,score=%d}", label, score);
        }
    }
}