package com.aaronicsubstances.cs_and_math.sorting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import static com.aaronicsubstances.cs_and_math.sorting.TournamentLoserTree.SortedItem;

public class MultiWayMerge {
    
    public static <T> Iterator<T> merge(List<Iterator<T>> sortedInputLists, Comparator<T> sortFunc) {
        // phase 1: gather first elements of each list.
        List<SortedItem<T>> heads = new ArrayList<>();
        int indexOfSortedList = 0;
        for (Iterator<T> inputList : sortedInputLists) {
            if (inputList.hasNext()) {
                T value = inputList.next();
                heads.add(new SortedItem<>(value, indexOfSortedList));
            }
            indexOfSortedList++;
        }

        // phase 2: build loser tree
        TournamentLoserTree<T> loserTreeDataStructure = new TournamentLoserTree<>();
        loserTreeDataStructure.start(heads, sortFunc);

        // phase 3: output winner nodes and replay games with new nodes,
        // until infinite marker node replaces all nodes
        // in originally built loser tree
        return new MultiWayMergeResult<>(sortedInputLists, sortFunc,
            loserTreeDataStructure);
    }

    private static class MultiWayMergeResult<T> implements Iterator<T> {
        private final List<Iterator<T>> sortedInputLists;
        private final Comparator<T> sortFunc;
        private final TournamentLoserTree<T> loserTreeDataStructure;

        public MultiWayMergeResult(List<Iterator<T>> sortedInputLists, Comparator<T> sortFunc,
                TournamentLoserTree<T> loserTreeDataStructure) {
            this.sortedInputLists = sortedInputLists;
            this.sortFunc = sortFunc;
            this.loserTreeDataStructure = loserTreeDataStructure;
        }

        @Override
        public boolean hasNext() {
            return loserTreeDataStructure.winnerExists();
        }

        @Override
        public T next() {
            SortedItem<T> winner = loserTreeDataStructure.getCurrentWinner();
            T nextResult = winner.value;
            
            // continue playing tournament with new item or with null.
            SortedItem<T> newItem = null;
            Iterator<T> inputList = sortedInputLists.get(winner.indexOfSortedList);
            if (inputList.hasNext()) {
                T value = inputList.next();
                newItem = new SortedItem<>(value, winner.indexOfSortedList);
            }
            loserTreeDataStructure.continueWith(newItem, sortFunc);
            
            return nextResult;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}