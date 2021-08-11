package com.aaronicsubstances.cs_and_math.sorting;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Based on https://en.wikipedia.org/wiki/K-way_merge_algorithm#Tournament_Tree
 */
public class TournamentLoserTree<T> {
    private final Comparator<T> sortFunc;
    private Node<T> winnerLeaf;

    public TournamentLoserTree(Comparator<T> sortFunc) {
        this.sortFunc = Objects.requireNonNull(sortFunc, "sortFunc");
        winnerLeaf = makeInfinityNode();
    }

    public boolean winnerExists() {
        return !winnerLeaf.isInfinity;
    }

    public T getCurrentWinner() {
        return winnerLeaf.value;
    }

    public void restart(List<T> initialElements) {
        Queue<Node<T>> leafNodes = new LinkedList<>();
        for (T item : initialElements) {
            leafNodes.add(Node.createLeaf(item, false));
        }
        buildTree(leafNodes);
    }

    public void continueWithoutReplacement() {
        winnerLeaf.value = null;
        winnerLeaf.isInfinity = true;
        replayGames(winnerLeaf.parent);
    }

    public void continueWithReplacement(T newElement) {
        winnerLeaf.value = newElement;
        winnerLeaf.isInfinity = false;
        replayGames(winnerLeaf.parent);
    }

    private void buildTree(Queue<Node<T>> initialLayer) {
        Queue<Node<T>> currentLayer;
        Queue<Node<T>> nextLayer = initialLayer;
        do {
            currentLayer = nextLayer;
            nextLayer = new LinkedList<>();
            while (!currentLayer.isEmpty()) {
                Node<T> firstElement = currentLayer.remove();
                Node<T> secondElement = currentLayer.poll();
                if (secondElement == null) {
                    secondElement = makeInfinityNode();
                }
                GamePlayResult<T> result = playGame(firstElement, secondElement, true);
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
            root.winner = null;
        }
        else {
            winnerLeaf = makeInfinityNode();
        }
    }

    private Node<T> makeInfinityNode() {
        return Node.createLeaf(null, true);
    }

    private void replayGames(Node<T> referenceNode) {
        // Run replacement selection algorithm
        while (referenceNode != null) {
            GamePlayResult<T> result = playGame(referenceNode, winnerLeaf,
                false);
            winnerLeaf = result.winner;
            Node<T> loserLeaf = result.loser;
            if (referenceNode.isLeaf()) {
                referenceNode.value = loserLeaf.value;
                referenceNode.isInfinity = loserLeaf.isInfinity;
            }
            else {
                referenceNode.loser = loserLeaf;
            }
            referenceNode = referenceNode.parent;
        }
    }

    /**
     * Compare nodes and determine the minimum (the "winner"). If there is a tie,
     * the first/leftmost node wins.
    */
    private GamePlayResult<T> playGame(Node<T> a, Node<T> b,
            boolean isGameBetweenWinners) {
        Node<T> winner = a;
        if (!a.isLeaf()) {
            winner = isGameBetweenWinners ? a.winner : a.loser;
        }
        Node<T> loser = b;
        if (!b.isLeaf()) {
            loser = isGameBetweenWinners ? b.winner : b.loser;
        }
        int result;
        if (winner.isInfinity || loser.isInfinity) {
            if (winner.isInfinity && loser.isInfinity) {
                // two infinity magnitudes found.
                // don't really care about who wins. 
                result = 0;
            }
            else if (winner.isInfinity) {
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
        public T value;
        public boolean isInfinity;

        // used only during building of tree
        public Node<T> winner;

        /**
         * Used to create leaf nodes of loser tree.
        */
        public static <T> Node<T> createLeaf(T value, boolean isInfinity) {
            Node<T> leaf = new Node<>();
            leaf.value = value;
            leaf.isInfinity = isInfinity;

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
            leftChild.parent = parent;
            leftChild.winner = null;
            rightChild.parent = parent;
            rightChild.winner = null;

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