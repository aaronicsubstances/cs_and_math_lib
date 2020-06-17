package com.aaronicsubstances.cs_and_math.regex;

import java.util.Map;

public interface RegexNode {
    Object accept(RegexNodeVisitor visitor);
    String toString(Map<String, String> opTokens);
    RegexNode generateCopy();
}