/* Copyright (C) 2013-2024 TU Dortmund University
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.incremental.dfa;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.Pair;
import net.automatalib.incremental.AdaptiveConstruction;
import net.automatalib.incremental.dfa.tree.AdaptiveDFATreeBuilder;
import net.automatalib.incremental.mealy.tree.AdaptiveMealyTreeBuilder;
import net.automatalib.ts.UniversalDTS;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedList;

@Test
public class AdaptiveDFATreeBuilderTest {

    private static final Alphabet<Character> TEST_ALPHABET = Alphabets.characters('a', 'b');

    // Confluence Bug
    private static final Word<Character> W_1 = Word.fromString("a");
    private static final Word<Character> W_2 = Word.fromString("b");
    private static final Word<Character> W_3 = Word.fromString("");

    private AdaptiveDFATreeBuilder<Character> adaptiveDFA =
            new AdaptiveDFATreeBuilder<>(TEST_ALPHABET);

    @Test
    public void testConfluenceBug() {
        adaptiveDFA.insert(W_1, true);
        adaptiveDFA.insert(W_2, true);
        adaptiveDFA.insert(W_3, true);

        Assert.assertFalse(adaptiveDFA.lookup(Word.fromString("aa")).getFirst());
        // reset for further tests
        this.adaptiveDFA = new AdaptiveDFATreeBuilder<>(TEST_ALPHABET);
    }

    @Test(dependsOnMethods = "testConfluenceBug")
    public void testLookup() {
        Assert.assertFalse(adaptiveDFA.hasDefinitiveInformation(W_1));
        Assert.assertFalse(adaptiveDFA.hasDefinitiveInformation(W_2));
        Assert.assertFalse(adaptiveDFA.hasDefinitiveInformation(W_3));

        adaptiveDFA.insert(W_1, false);
        Assert.assertTrue(adaptiveDFA.hasDefinitiveInformation(W_1));
        Assert.assertFalse(adaptiveDFA.hasDefinitiveInformation(W_1.append('a')));

        Assert.assertTrue(adaptiveDFA.lookup(W_1).getFirst());
        Assert.assertFalse(adaptiveDFA.lookup(W_1).getSecond());

        adaptiveDFA.insert(W_2, true);
        Assert.assertTrue(adaptiveDFA.hasDefinitiveInformation(W_1));
        Assert.assertTrue(adaptiveDFA.hasDefinitiveInformation(W_2));
        Assert.assertFalse(adaptiveDFA.hasDefinitiveInformation(W_3));

        Assert.assertTrue(adaptiveDFA.lookup(W_2).getFirst());
        Assert.assertTrue(adaptiveDFA.lookup(W_2).getSecond());

        adaptiveDFA.insert(W_1, true);
        Assert.assertTrue(adaptiveDFA.hasDefinitiveInformation(W_1));
        Assert.assertTrue(adaptiveDFA.hasDefinitiveInformation(W_2));
        Assert.assertFalse(adaptiveDFA.hasDefinitiveInformation(W_3));

        Assert.assertTrue(adaptiveDFA.lookup(W_1).getFirst());
        Assert.assertTrue(adaptiveDFA.lookup(W_1).getSecond());
    }

    @Test(dependsOnMethods = "testLookup")
    public void testInsertSame() {
        adaptiveDFA.insert(W_1, true);
    }

    @Test
    public void testAges() {
        LinkedList<Pair<Word<Character>, Boolean>> words = new LinkedList<>();
        words.add(Pair.of(W_1, true));
        words.add(Pair.of(W_2, false));
        words.add(Pair.of(W_3, false));

        for (Pair<Word<Character>, Boolean> word : words) {
            adaptiveDFA.insert(word.getFirst(), word.getSecond());
        }
        Assert.assertEquals(W_1, adaptiveDFA.getOldestInput());
        adaptiveDFA.insert(W_1, false);
        Assert.assertEquals(W_2, adaptiveDFA.getOldestInput());
    }

    @Test(dependsOnMethods = "testLookup")
    public void testFindSeparatingWord() {
        CompactDFA<Character> testDFA = new CompactDFA<>(TEST_ALPHABET);

        int s0 = testDFA.addInitialState(false);
        int s1 = testDFA.addState(true);
        int s2 = testDFA.addState(false);
        int s3 = testDFA.addState(true);


        testDFA.addTransition(s1, 'b', s3);
        testDFA.addTransition(s1, 'a', s0);

        Word<Character> sepWord;
        sepWord = adaptiveDFA.findSeparatingWord(testDFA, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = adaptiveDFA.findSeparatingWord(testDFA, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.fromString("a"));

        testDFA.addTransition(s0, 'a', s1);
        testDFA.addTransition(s0, 'b', s1);
        sepWord = adaptiveDFA.findSeparatingWord(testDFA, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = adaptiveDFA.findSeparatingWord(testDFA, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);
    }


    @Test
    public void testEmpty() {
        final AdaptiveConstruction.DFABuilder<Character> incDFA = new AdaptiveDFATreeBuilder<>(
                TEST_ALPHABET);

        Assert.assertNull(incDFA.lookup(W_1).getSecond());
        Assert.assertNull(incDFA.getOldestInput());
    }
}
