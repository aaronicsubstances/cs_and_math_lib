package com.aaronicsubstances.cs_and_math.sorting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MultiWayMerge {
    
    public static <T> Iterator<T> merge(List<Iterator<T>> sortedInputLists, Comparator<T> sortFunc) {
        // phase 1: gather first elements of each list.
        // and modify sort function to guarantee stable sorting
        // between input lists.
        Comparator<SortedItem<T>> effectiveSortFunc = (a, b) -> {
            int result = sortFunc.compare(a.value, b.value);            
            if (result == 0) {
                // ensure stable sort with assumption that,
                // when input sorted lists are concatenated, the result is the original
                // unsorted list.
                if (a.indexOfSortedList > b.indexOfSortedList) {
                    result = 1;
                }
                else if (a.indexOfSortedList < b.indexOfSortedList) {
                    result = -1;
                }
            }
            return result;
        };
        List<SortedItem<T>> heads = new ArrayList<>();
        for (int i = 0; i < sortedInputLists.size(); i++) {
            Iterator<T> inputList = sortedInputLists.get(i);
            if (inputList.hasNext()) {
                T value = inputList.next();
                heads.add(new SortedItem<>(value, i));
            }
        }

        // phase 2: build tournament tree
        TournamentLoserTree<SortedItem<T>> tournamentTree = 
            new TournamentLoserTree<>(effectiveSortFunc);
        tournamentTree.restart(heads);

        // phase 3: output winner nodes and replay games with new nodes,
        // until infinite marker node replaces all nodes
        // in originally built loser tree
        return new MultiWayMergeResult<>(sortedInputLists,
            tournamentTree);
    }

    private static class SortedItem<T> {
        public T value;
        public int indexOfSortedList;

        public SortedItem(T value, int indexOfSortedList) {
            this.value = value;
            this.indexOfSortedList = indexOfSortedList;
        }
    }

    private static class MultiWayMergeResult<T> implements Iterator<T> {
        private final List<Iterator<T>> sortedInputLists;
        private final TournamentLoserTree<SortedItem<T>> tournamentTree;

        public MultiWayMergeResult(List<Iterator<T>> sortedInputLists,
                TournamentLoserTree<SortedItem<T>> tournamentTree) {
            this.sortedInputLists = sortedInputLists;
            this.tournamentTree = tournamentTree;
        }

        @Override
        public boolean hasNext() {
            return tournamentTree.winnerExists();
        }

        @Override
        public T next() {
            SortedItem<T> winner = tournamentTree.getCurrentWinner();
            T nextResult = winner.value;
            
            // continue playing tournament with or without a next item.
            Iterator<T> inputList = sortedInputLists.get(winner.indexOfSortedList);
            if (inputList.hasNext()) {
                T value = inputList.next();
                SortedItem<T> newItem = new SortedItem<>(value, winner.indexOfSortedList);
                tournamentTree.continueWithReplacement(newItem);
            }
            else {
                tournamentTree.continueWithoutReplacement();
            }
            
            return nextResult;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}