package com.aaronicsubstances.cs_and_math.parsing.pratt.expressions;

/**
 * Interface for all expression AST node classes.
 */
public interface Expression {
  
    /**
     * Pretty-print the expression to a string.
     */
    void print(StringBuilder builder);
}
