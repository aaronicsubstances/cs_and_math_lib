package com.aaronicsubstances.cs_and_math.sorting;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import static com.aaronicsubstances.cs_and_math.sorting.TournamentLoserTree.Node;

public class MultiWayMerge {
    
    public static <T> Iterator<T> merge(List<Iterator<T>> sortedInputLists, Comparator<T> sortFunc) {
        // phase 1: gather first elements of each list.
        Queue<Node<T>> heads = new LinkedList<>();
        for (int i = 0; i < sortedInputLists.size(); i++) {
            Iterator<T> inputList = sortedInputLists.get(i);
            if (inputList.hasNext()) {
                T value = inputList.next();
                Node<T> n = new Node<>(value, i);
                heads.add(n);
            }
        }

        // phase 2: build loser tree
        TournamentLoserTree<T> loserTreeDataStructure = new TournamentLoserTree<>();
        loserTreeDataStructure.buildTree(heads, sortFunc);

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
            return loserTreeDataStructure.winnerLeaf != null &&
                loserTreeDataStructure.winnerLeaf.index >= 0;
        }

        @Override
        public T next() {
            Node<T> winnerLeaf = loserTreeDataStructure.winnerLeaf;
            T nextResult = winnerLeaf.value;
            
            // Run replacement selection algorithm
            Node<T> tempNodeLeaf = null;
            Iterator<T> inputList = sortedInputLists.get(winnerLeaf.index);
            if (inputList.hasNext()) {
                T value = inputList.next();
                tempNodeLeaf = new Node<>(value, winnerLeaf.index);
            }
            loserTreeDataStructure.replayGames(winnerLeaf, tempNodeLeaf, sortFunc);
            
            return nextResult;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}