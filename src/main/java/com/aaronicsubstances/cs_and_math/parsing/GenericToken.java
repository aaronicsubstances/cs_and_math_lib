package com.aaronicsubstances.cs_and_math.parsing;

/**
 * Base of tokens for custom lexers and recursive-descent parsers
 */
public class GenericToken {
    public int type;
    public String text;

    public GenericToken() {
    }

    public GenericToken(int type, String text) {
        this.type = type;
        this.text = text;
    }
}