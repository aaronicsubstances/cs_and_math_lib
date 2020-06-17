package com.aaronicsubstances.cs_and_math;

public class TestArg<T> {
    public final T value;

    public TestArg(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        // generate a shorter name than Object.toString()
        return String.format("%s@%x",
            getClass().getSimpleName(), hashCode());
    }
}