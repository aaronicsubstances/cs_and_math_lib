package com.aaronicsubstances.cs_and_math.sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

public class TournamentLoserTreeTest {
    private final int randomTestCount = 100;
    private final int maxInputSize = 100;
    private final TournamentLoserTree<Integer> instance = new TournamentLoserTree<>(Integer::compare);
    private final Random randGen = new Random();

    @Test
    public void testRestart() {
        for (int i = 0; i < randomTestCount; i++) {
            // arrange
            int randLength = randGen.nextInt(maxInputSize);
            List<Integer> input = new ArrayList<>();
            for (int j = 0; j < randLength; j++) {
                input.add(randGen.nextInt());
            }

            // act
            instance.restart(input);

            // assert
            if (!input.isEmpty()) {
                Integer expected = Collections.min(input);
                assertTrue(instance.winnerExists());
                Integer actual = instance.getCurrentWinner();
                assertEquals(actual, expected);
            }
            else {
                assertFalse(instance.winnerExists());
            }
        }
    }

    @Test
    public void testContinueWithReplacement() {
        for (int i = 0; i < randomTestCount; i++) {
            // arrange
            int randLength = randGen.nextInt(maxInputSize);
            List<Integer> input = new ArrayList<>();
            for (int j = 0; j < randLength; j++) {
                input.add(randGen.nextInt());
            }

            // act
            instance.restart(input);

            for (int j = 0; j < randLength; j++) {                
                // determine expected.
                if (!input.isEmpty()) {
                    int previousMin = Collections.min(input);
                    assertTrue(input.remove((Object)previousMin));
                }
                int replacement = randGen.nextInt();
                input.add(replacement);
                Integer expected = Collections.min(input);

                // act
                instance.continueWithReplacement(replacement); 

                // assert
                assertTrue(instance.winnerExists());
                Integer actual = instance.getCurrentWinner();
                assertEquals(actual, expected);
            }
        }
    }

    @Test
    public void testContinueWithoutReplacement() {
        Random randGen = new Random();
        for (int i = 0; i < randomTestCount; i++) {
            // arrange
            int randLength = randGen.nextInt(maxInputSize);
            List<Integer> input = new ArrayList<>();
            for (int j = 0; j < randLength; j++) {
                input.add(randGen.nextInt());
            }

            // act
            instance.restart(input);

            // assert
            List<Integer> expected = new ArrayList<>(input);
            expected.sort(null);
            List<Integer> actual = new ArrayList<>();
            while (instance.winnerExists()) {
                actual.add(instance.getCurrentWinner());
                instance.continueWithoutReplacement();
            }
            assertFalse(instance.winnerExists());
            assertEquals(actual, expected);
        }
    }
}