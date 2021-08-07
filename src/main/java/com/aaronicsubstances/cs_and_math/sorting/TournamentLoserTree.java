package com.aaronicsubstances.cs_and_math.sorting;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Based on https://en.wikipedia.org/wiki/K-way_merge_algorithm#Tournament_Tree
 */
public class TournamentLoserTree<T> {

    public static class SortedItem<T> {
        public T value;
        public int indexOfSortedList;

        public SortedItem(T value, int indexOfSortedList) {
            this.value = value;
            this.indexOfSortedList = indexOfSortedList;
        }
    }

    private Node<T> winnerLeaf;
    private final Node<T> infinityMarkerNode = Node.createLeaf(null, -1);

    public TournamentLoserTree() {
    }

    public boolean winnerExists() {
        return winnerLeaf != null && winnerLeaf.index >= 0;
    }

    public SortedItem<T> getCurrentWinner() {
        return new SortedItem<>(winnerLeaf.value, winnerLeaf.index);
    }

    public void start(List<SortedItem<T>> headElements, Comparator<T> sortFunc) {
        Queue<Node<T>> headNodes = new LinkedList<>();
        for (SortedItem<T> item : headElements) {
            headNodes.add(Node.createLeaf(item.value, item.indexOfSortedList));
        }
        buildTree(headNodes, sortFunc);
    }

    public void continueWith(SortedItem<T> newElement, Comparator<T> sortFunc) {
        // Run replacement selection algorithm
        Node<T> tempNodeLeaf = null;
        if (newElement != null) {
            tempNodeLeaf = Node.createLeaf(newElement.value, newElement.indexOfSortedList);
        }
        replayGames(winnerLeaf, tempNodeLeaf, sortFunc);
    }

    private void buildTree(Queue<Node<T>> initialLayer, Comparator<T> sortFunc) {
        Queue<Node<T>> currentLayer;
        Queue<Node<T>> nextLayer = initialLayer;
        do {
            currentLayer = nextLayer;
            nextLayer = new LinkedList<>();
            while (!currentLayer.isEmpty()) {
                Node<T> firstElement = currentLayer.remove();
                Node<T> secondElement = currentLayer.poll();
                GamePlayResult<T> result = playGame(firstElement, secondElement, sortFunc, true);
                Node<T> parent = Node.createParent(firstElement, secondElement,
                    result.winner, result.loser);
                nextLayer.add(parent);
            }
        } while (nextLayer.size() > 1);
        
        // no need to save root, since replay games uses null parent to
        // detect top of tree.
        Node<T> root = nextLayer.poll();
        if (root != null) {
            winnerLeaf = root.winner;
        }
        else {
            winnerLeaf = null;
        }
    }

    private void replayGames(Node<T> referenceNode, Node<T> contenderNode,
            Comparator<T> sortFunc) {
        while (referenceNode != null) {
            GamePlayResult<T> result = playGame(referenceNode, contenderNode, sortFunc,
                false);
            winnerLeaf = result.winner;
            Node<T> loserLeaf = result.loser;
            if (referenceNode.isLeaf()) {
                referenceNode.value = loserLeaf.value;
                referenceNode.index = loserLeaf.index;
            }
            else {
                referenceNode.loser = loserLeaf;
            }
            referenceNode = referenceNode.parent;
            contenderNode = winnerLeaf;
        }
    }

    /**
     * Compare nodes and determine the minimum (the "winner"). If there is a tie,
     * the first/leftmost node wins. 
    */
    private GamePlayResult<T> playGame(Node<T> a, Node<T> b, Comparator<T> sortFunc,
            boolean isGameBetweenWinners) {        
        Node<T> winner = a;
        if (a != null && !a.isLeaf()) {
            winner = isGameBetweenWinners ? a.winner : a.loser;
        }
        Node<T> loser = b;
        if (b != null && !b.isLeaf()) {
            loser = isGameBetweenWinners ? b.winner : b.loser;
        }
        // assign special node for infinite magnitude.
        if (winner == null) {
            winner = infinityMarkerNode;
        }
        if (loser == null) {
            loser = infinityMarkerNode;
        }
        int result;
        if (winner.index < 0 || loser.index < 0) {
            if (winner.index < 0 && loser.index < 0) {
                // two infinity magnitudes found.
                // don't really care about who wins. 
                result = 0;
            }
            else if (winner.index < 0) {
                // infinity always loses to normal values.
                result = 1;
            }
            else {
                // normal values always win over infinity
                result = -1;
            }
        }
        else {
            result = sortFunc.compare(winner.value, loser.value);
            if (result == 0) {
                // ensure stable sort with assumption that,
                // when input sorted lists are concatenated, the result is the original
                // unsorted list.
                if (winner.index > loser.index) {
                    result = 1;
                }
                else if (winner.index < loser.index) {
                    result = -1;
                }
            }
        }
        if (result > 0) {
            // swap
            Node<T> temp = loser;
            loser = winner;
            winner = temp;
        }
        return new GamePlayResult<>(winner, loser);
    }

    private static class Node<T> {
        public Node<T> parent, loser;

        // used only by leaf nodes
        public int index;
        public T value;

        // used only during building of tree
        public Node<T> winner;

        /**
         * Used to create leaf nodes of loser tree.
        */
        public static <T> Node<T> createLeaf(T value, int index) {
            Node<T> leaf = new Node<>();
            leaf.value = value;
            leaf.index = index;

            return leaf;
        }

        /**
         * Used to create internal nodes of loser tree.
         */
        public static <T> Node<T> createParent(Node<T> leftChild,
                Node<T> rightChild, Node<T> winner, Node<T> loser) {
            Node<T> parent = new Node<>();
            parent.winner = winner;
            parent.loser = loser;            

            // set parent links and clear winner fields 
            // since they are no longer needed
            if (leftChild != null) {
                leftChild.parent = parent;
                leftChild.winner = null;
            }
            if (rightChild != null) {
                rightChild.parent = parent;
                rightChild.winner = null;
            }

            return parent;
        }

        public boolean isLeaf() {
            return loser == null;
        }
    }

    private static class GamePlayResult<T> {
        public Node<T> winner;
        public Node<T> loser; 

        public GamePlayResult(Node<T> winner, Node<T> loser) {
            this.winner = winner;
            this.loser = loser;
        }
    }
}