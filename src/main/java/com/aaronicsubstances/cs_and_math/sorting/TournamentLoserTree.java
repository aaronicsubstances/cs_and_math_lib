package com.aaronicsubstances.cs_and_math.sorting;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Based on https://en.wikipedia.org/wiki/K-way_merge_algorithm#Tournament_Tree
 */
public class TournamentLoserTree<T> {
    public Node<T> winnerLeaf;
    private Node<T> root, infinityMarkerNode;

    public TournamentLoserTree() {
    }

    void buildTree(Queue<Node<T>> elements, Comparator<T> sortFunc) {
        infinityMarkerNode = new Node<>(null, -1);
        winnerLeaf = infinityMarkerNode;
        Queue<Node<T>> currentLayer;
        Queue<Node<T>> nextLayer = elements;
        do {
            currentLayer = nextLayer;
            nextLayer = new LinkedList<>();
            while (!currentLayer.isEmpty()) {
                Node<T> firstElement = currentLayer.remove();
                Node<T> secondElement = currentLayer.poll();
                GamePlayResults<T> results = playGame(firstElement, secondElement, sortFunc);
                Node<T> parent = new Node<>(firstElement, secondElement, results.loser,
                    results.winner);
                nextLayer.add(parent);

                // clear these winner fields since they are no longer needed
                firstElement.winner = null;
                secondElement.winner = null;
            }
        } while (nextLayer.size() > 1);
        root = nextLayer.poll();
        if (root != null) {
            winnerLeaf = root.winner;
            // clear since no longer needed.
            root.winner = null;
        }
    }

    /**
     * Compare nodes and determine the minimum (the "winner"). If there is a tie,
     * the first/leftmost node wins. 
    */
    private GamePlayResults<T> playGame(Node<T> a, Node<T> b, Comparator<T> sortFunc) {        
        Node<T> winner = a.isLeaf() ? a : a.loser;
        Node<T> loser;
        if (b != null) {
            loser = b.isLeaf() ? b : b.loser;
        }
        else {
            // assign special node for infinite magnitude.
            loser = infinityMarkerNode;
        }
        if (loser != infinityMarkerNode) {
            int result = sortFunc.compare(winner.value, loser.value);
            if (result > 0) {
                // swap
                Node<T> temp = loser;
                loser = winner;
                winner = temp;
            }
        }
        return new GamePlayResults<>(winner, loser);
    }

    void replayGames(Node<T> referenceNode, Node<T> contenderNode,
            Comparator<T> sortFunc) {
        while (referenceNode != root) {
            GamePlayResults<T> results = playGame(referenceNode, contenderNode, sortFunc);
            winnerLeaf = results.winner;
            Node<T> loserLeaf = results.loser;
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

    static class Node<T> {
        public Node<T> parent, loser;

        // used only by leaf nodes
        public int index;
        public T value;

        // used only during building of tree
        public Node<T> winner;

        /**
         * Used to create leaf nodes of loser tree.
        */
        public Node(T value, int index) {
            this.value = value;
            this.index = index;
        }

        /**
         * Used to create internal nodes of loser tree.
         */
        public Node(Node<T> leftChild, Node<T> rightChild, Node<T> loser, Node<T> winner) {
            leftChild.parent = this;
            if (rightChild != null) {
                rightChild.parent = this;
            }
            this.loser = loser;
            this.winner = winner;
        }

        public boolean isLeaf() {
            return loser == null;
        }
    }

    private static class GamePlayResults<T> {
        public Node<T> winner;
        public Node<T> loser; 

        public GamePlayResults(Node<T> winner, Node<T> loser) {
            this.winner = winner;
            this.loser = loser;
        }
    }
}