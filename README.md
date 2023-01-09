# My Library of Computer Science and Discrete Mathematics Algorithm and Data Structures

This Java library is a selected implementation of computer science and discrete mathematics algorithms and data structures.

## Includes

  1. Implementation of discrete-event simulation
  1. Implementation of external sorting.
  1. Implementation of multi-way merge algorithm (a.k.a. k-way merge).
  1. Implementation of tournament loser tree for use with k-way algorithm.
  1. Implementation of Unix diff normal format
  1. Generating Permutations and Combinations
  1. Generating Cartesian Products
  1. Dijsktra Shortest-Path Graph Algorithm
  1. Class for modelling Finite State Automaton (FSA)
  1. Printing State Table Representation of FSA
  1. Printing State Diagram Representation of FSA in Graphviz DOT notation
  1. Determining equivalence of two FSAs (disregarding state numbering)
  1. Converting ASTs (abstract syntax tree) of Type-3 Grammars (regular expressions) to Non-Deterministic Finite State Automaton (NFA)
  1. Converting NFA to DFA (deterministic finite state automaton)
  1. Simulating DFA
  1. Simulating NFA (ie directly without converting to DFA)
  1. Base classes for implementing Pratt Parser
  1. Implementing Source Maps (ie recording changes made to an original file to obtain changed file, and later mapping positions in changed file back to positions in original file)
  

## Build Instructions

   * Ensure JDK 8 is installed locally, and that JAVA_HOME environment variable is set up properly.
   * Also ensure JAVA_HOME/bin folder with **java** and **javac** executables is placed on the system path.
   * Clone repository and run `gradlew build` from root of project. Can also run `gradlew clean build` instead (Gradle 5.6.4 will be downloaded and used by the commands). This test and builds the project.

## Tests Requiring Manual Inspection

The following tests generate standard output/error messages which can be verified for correctness:

   * cs_and_math.MathAlgorithmsTest.testShuffleList
   * cs_and_math.parsing.LexerSupportTest.{testParseDecimalStringForError,testParseHexadecimalStringForError}


