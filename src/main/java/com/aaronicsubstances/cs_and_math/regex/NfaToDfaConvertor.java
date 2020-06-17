package com.aaronicsubstances.cs_and_math.regex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aaronicsubstances.cs_and_math.FiniteStateAutomaton;

/**
 * Converts an NFA to a DFA.
 * <p>
 * Implements Algorithm 3.20 of Compilers - Principles, Techniques and Tools
 * (aka Dragon Book), 2nd edition.
 * Also known as subset construction of DFA from NFA.
 * 
 */
public class NfaToDfaConvertor {
    private final FiniteStateAutomaton nfa;
    private final Map<Integer, Set<Integer>> emptyStringGraph;

    private List<Set<Integer>> nfaStateSubsets;
    
    /**
     * Creates new instance for conversion
     * @param nfa NFA to convert. The alphabet property must be set since it is needed by
     * this algorithm.
     */
    public NfaToDfaConvertor(FiniteStateAutomaton nfa) {
        this.nfa = nfa;

        // build adjacency list type of graph with empty string transitions 
        emptyStringGraph = buildEmptyStringGraph(nfa);
    }

    public List<Set<Integer>> getNfaStateSubsets() {
        return nfaStateSubsets;
    }

    /**
     * Converts NFA to DFA.
     * @param ignoreEmptyState whether or the DFA state corresponding to empty states of
     * NFA should be ignored if encountered. If false, then it will be treated similarly to
     * all other states; else if true, then even though it will be included in DFA states, 
     * all transitions from or to that state will be omitted.
     * @return DFA
     */
    public FiniteStateAutomaton convert(boolean ignoreEmptyState) {
        Set<Integer> dfaStates = FiniteStateAutomaton.newSet();
        Set<Integer> dfaFinalStates = FiniteStateAutomaton.newSet();
        Map<Integer, Map<Integer, Integer>> dfaTransitionTable = new HashMap<>();
        nfaStateSubsets = new ArrayList<>();
        Set<Integer> startNfaStateSubset = emptyStringClosure(emptyStringGraph,
            FiniteStateAutomaton.newSet(nfa.getStartState()));
        nfaStateSubsets.add(startNfaStateSubset);
        int processedCount = 0;
        while (processedCount < nfaStateSubsets.size()) {
            int nextDState = processedCount;
            Set<Integer> nextNfaStateSubset = nfaStateSubsets.get(nextDState);
            processedCount++;

            dfaStates.add(nextDState);
            for (int s : nextNfaStateSubset) {
                if (nfa.getFinalStates().contains(s)) {
                    dfaFinalStates.add(nextDState);
                    break;
                }
            }

            // skip transitions from empty states if requested.
            if (ignoreEmptyState && nextNfaStateSubset.isEmpty()) {
                continue;
            }
            
            // for each input symbol discover new states
            Map<Integer, Integer> dfaOutStateTransitions = new HashMap<>();
            dfaTransitionTable.put(nextDState, dfaOutStateTransitions);
            for (int c : nfa.getAlphabet()) {
                Set<Integer> discoveredNfaStateSubset = move(nfa, nextNfaStateSubset, c);
                discoveredNfaStateSubset = emptyStringClosure(emptyStringGraph,
                    discoveredNfaStateSubset);
                int discoveredDState = nfaStateSubsets.indexOf(discoveredNfaStateSubset);
                if (discoveredDState == -1) {
                    discoveredDState = nfaStateSubsets.size();
                    nfaStateSubsets.add(discoveredNfaStateSubset);
                }
                if (ignoreEmptyState && discoveredNfaStateSubset.isEmpty()) {
                    // skip transitions to empty state if requested.
                }
                else {
                    dfaOutStateTransitions.put(c, discoveredDState);
                }
            }
        }
        FiniteStateAutomaton dfa = new FiniteStateAutomaton(nfa.getAlphabet(), 
            dfaStates, 0, dfaFinalStates, null, dfaTransitionTable);
        return dfa;
    }

    static Set<Integer> move(FiniteStateAutomaton nfa, Set<Integer> states, int c) {
        Set<Integer> nextStates = FiniteStateAutomaton.newSet();
        for (int s : states) {
            Map<Integer, Set<Integer>> stateOutTransitions = nfa.getNfaTransitionTable().get(s);
            if (stateOutTransitions != null &&
                    stateOutTransitions.containsKey(c)) {
                nextStates.addAll(stateOutTransitions.get(c));
            }
        }
        return nextStates;
    }

    static Map<Integer, Set<Integer>> buildEmptyStringGraph(FiniteStateAutomaton nfa) {
        Map<Integer, Set<Integer>> emptyStringGraph = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Set<Integer>>> entry : 
                nfa.getNfaTransitionTable().entrySet()) {
            if (entry.getValue().containsKey(FiniteStateAutomaton.NULL_SYMBOL)) {
                emptyStringGraph.put(entry.getKey(), entry.getValue().get(
                    FiniteStateAutomaton.NULL_SYMBOL));
            }
        }
        return emptyStringGraph;
    }

    static Set<Integer> emptyStringClosure(
            Map<Integer, Set<Integer>> emptyStringGraph,
            Set<Integer> startStates) {
        // NB: resembles breadth first search graph algorithm.
        Set<Integer> closureResult = FiniteStateAutomaton.newSet();
        closureResult.addAll(startStates);
        LinkedList<Integer> processedStates = new LinkedList<>(startStates);
        while (!processedStates.isEmpty()) {
            int t = processedStates.removeFirst();
            if (emptyStringGraph.containsKey(t)) {
                for (int u : emptyStringGraph.get(t)) {
                    if (!closureResult.contains(u)) {
                        closureResult.add(u);
                        processedStates.addLast(u);
                    }
                }
            }
        }
        return closureResult;
    }
}