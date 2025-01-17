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

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.common.util.IOUtil;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.IncrementalConstruction;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.ts.UniversalDTS;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public abstract class AbstractIncrementalPCDFABuilderTest {

    private static final Alphabet<Character> TEST_ALPHABET = Alphabets.characters('a', 'c');

    private static final Word<Character> W_1 = Word.fromString("abc");
    private static final Word<Character> W_2 = Word.fromString("acb");
    private static final Word<Character> W_3 = Word.fromString("ac");

    private IncrementalConstruction.DFABuilder<Character> incPcDfa;

    @BeforeClass
    public void setUp() {
        this.incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);
    }

    protected abstract <I> IncrementalConstruction.DFABuilder<I> createIncrementalPCDFABuilder(Alphabet<I> alphabet);

    protected abstract String getDOTResource();

    @Test
    public void testConfluenceBug() {
        Word<Character> wB1 = Word.fromString("aaa");
        Word<Character> wB2 = Word.fromString("bba");
        Word<Character> wB3 = Word.fromString("aabaa");

        incPcDfa.insert(wB1, true);
        incPcDfa.insert(wB2, true);
        incPcDfa.insert(wB3, true);

        Assert.assertEquals(incPcDfa.lookup(Word.fromString("aababaa")).getSecond(), null);
        this.incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);
    }

    @Test(dependsOnMethods = "testConfluenceBug")
    public void testLookup() {
        Assert.assertEquals(incPcDfa.lookup(W_1).getSecond(), null);
        Assert.assertEquals(incPcDfa.lookup(W_2).getSecond(), null);
        Assert.assertEquals(incPcDfa.lookup(W_3).getSecond(), null);

        incPcDfa.insert(Word.epsilon(), true);
        Assert.assertEquals(incPcDfa.lookup(W_1).getSecond(), null);
        Assert.assertEquals(incPcDfa.lookup(W_2).getSecond(), null);
        Assert.assertEquals(incPcDfa.lookup(W_3).getSecond(), null);

        incPcDfa.insert(W_1, true);
        Assert.assertEquals(incPcDfa.lookup(W_1).getSecond(), true);
        Assert.assertEquals(incPcDfa.lookup(W_1.prefix(2)).getSecond(), true);
        Assert.assertEquals(incPcDfa.lookup(W_1.prefix(1)).getSecond(), true);
        Assert.assertEquals(incPcDfa.lookup(W_1.prefix(0)).getSecond(), true);

        Assert.assertEquals(incPcDfa.lookup(W_2).getSecond(), null);
        Assert.assertEquals(incPcDfa.lookup(W_3).getSecond(), null);

        incPcDfa.insert(W_2, false);
        Assert.assertEquals(incPcDfa.lookup(W_1).getSecond(), true);
        Assert.assertEquals(incPcDfa.lookup(W_2).getSecond(), false);
        Assert.assertEquals(incPcDfa.lookup(W_2.append('a')).getSecond(), false);
        Assert.assertEquals(incPcDfa.lookup(W_3).getSecond(), null);

        Assert.assertEquals(incPcDfa.lookup(W_1.prefix(1)).getSecond(), true);
        Assert.assertEquals(incPcDfa.lookup(W_2.prefix(1)).getSecond(), true);

        incPcDfa.insert(W_3, true);
        Assert.assertEquals(incPcDfa.lookup(W_1).getSecond(), true);
        Assert.assertEquals(incPcDfa.lookup(W_2).getSecond(), false);
        Assert.assertEquals(incPcDfa.lookup(W_3).getSecond(), true);

        Assert.assertEquals(incPcDfa.lookup(W_1.prefix(2)).getSecond(), true);
        Assert.assertEquals(incPcDfa.lookup(W_2.prefix(1)).getSecond(), true);
        Assert.assertEquals(incPcDfa.lookup(W_3.append('a')).getSecond(), null);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testInsertSame() {
        int oldSize = incPcDfa.asGraph().size();
        incPcDfa.insert(W_1, true);
        Assert.assertEquals(incPcDfa.asGraph().size(), oldSize);
    }

    @Test(expectedExceptions = ConflictException.class, dependsOnMethods = "testLookup")
    public void testConflict() {
        incPcDfa.insert(W_1, false);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testFindSeparatingWord() {
        CompactDFA<Character> testDfa = new CompactDFA<>(TEST_ALPHABET);
        int s0 = testDfa.addInitialState(true);
        int s1 = testDfa.addState(true);
        int s2 = testDfa.addState(true);
        int s3 = testDfa.addState(true);

        testDfa.addTransition(s0, 'a', s1);
        testDfa.addTransition(s1, 'b', s2);
        testDfa.addTransition(s2, 'c', s3);

        Word<Character> sepWord;
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.fromString("ac"));

        int s4 = testDfa.addState(true);
        testDfa.addTransition(s1, 'c', s4);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);

        int s5 = testDfa.addState(false);
        testDfa.addTransition(s4, 'b', s5);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);

        int s6 = testDfa.addState(false);
        testDfa.addTransition(s3, 'a', s6);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);

        int s7 = testDfa.addState(true);
        testDfa.addTransition(s5, 'a', s7);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertEquals(sepWord, Word.fromString("acba"));
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.fromString("acba"));
    }

    @Test(dependsOnMethods = "testLookup")
    public void testVisualization() throws IOException {
        final StringWriter dotWriter = new StringWriter();
        final StringWriter expectedWriter = new StringWriter();

        try (Reader reader = IOUtil.asBufferedUTF8Reader(AbstractIncrementalPCDFABuilderTest.class.getResourceAsStream(
                getDOTResource()))) {

            GraphDOT.write(incPcDfa.asGraph(), dotWriter);
            IOUtil.copy(reader, expectedWriter);

            Assert.assertEquals(dotWriter.toString(), expectedWriter.toString());
        }
    }

    @Test(dependsOnMethods = "testLookup")
    public void testTSView() {
        testTSViewInternal(incPcDfa.asTransitionSystem());
    }

    private static <S> void testTSViewInternal(UniversalDTS<S, Character, ?, Acceptance, Void> view) {
        final S s1 = view.getState(W_1);
        final S s2 = view.getState(W_2);
        final S s3 = view.getState(W_3);

        Assert.assertTrue(view.getStateProperty(s1).toBoolean());
        Assert.assertFalse(view.getStateProperty(s2).toBoolean());
        Assert.assertTrue(view.getStateProperty(s3).toBoolean());
    }

    @Test(dependsOnMethods = "testLookup")
    public void testNewInputSymbol() {
        final GrowingAlphabet<Character> alphabet = new GrowingMapAlphabet<>(TEST_ALPHABET);
        final IncrementalConstruction.DFABuilder<Character> growableBuilder = createIncrementalPCDFABuilder(alphabet);

        growableBuilder.addAlphabetSymbol('d');
        growableBuilder.addAlphabetSymbol('d');

        final Word<Character> input1 = Word.fromString("dcba");

        growableBuilder.insert(input1, true);

        Assert.assertTrue(growableBuilder.hasDefinitiveInformation(input1));
        Assert.assertEquals(growableBuilder.lookup(input1).getSecond(), true);
        Assert.assertEquals(growableBuilder.lookup(input1.prefix(2)).getSecond(), true);

        final Word<Character> input2 = Word.fromString("dddd");

        Assert.assertFalse(growableBuilder.hasDefinitiveInformation(input2));
        Assert.assertEquals(growableBuilder.lookup(input2).getSecond(), null);

        growableBuilder.insert(input2, false);
        Assert.assertEquals(growableBuilder.lookup(input2).getSecond(), false);
        Assert.assertEquals(growableBuilder.lookup(input2.append('d')).getSecond(), false);
    }

    @Test
    public void testCounterexampleOfLengthOne() {
        final IncrementalConstruction.DFABuilder<Character> incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);
        incPcDfa.insert(Word.fromLetter('a'), true);

        final CompactDFA<Character> dfa = new CompactDFA<>(TEST_ALPHABET);
        final Integer q0 = dfa.addInitialState(true);
        final Integer q1 = dfa.addState(false);

        dfa.addTransition(q0, 'a', q1);

        final Word<Character> ce = incPcDfa.findSeparatingWord(dfa, TEST_ALPHABET, false);
        Assert.assertNotNull(ce);
    }

    @Test
    public void testRejectAll() {
        final IncrementalConstruction.DFABuilder<Character> incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);

        incPcDfa.insert(Word.epsilon(), false);

        Assert.assertEquals(incPcDfa.lookup(W_1).getSecond(), false);
        Assert.assertEquals(incPcDfa.lookup(W_2).getSecond(), false);
        Assert.assertEquals(incPcDfa.lookup(W_3).getSecond(), false);

        Assert.assertThrows(ConflictException.class, () -> incPcDfa.insert(W_1, true));
        Assert.assertThrows(ConflictException.class, () -> incPcDfa.insert(W_2, true));
        Assert.assertThrows(ConflictException.class, () -> incPcDfa.insert(W_3, true));
    }

    @Test
    public void testLateSink() {
        final IncrementalConstruction.DFABuilder<Character> incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);

        Word<Character> w1 = Word.fromString("abc");
        Word<Character> w2 = Word.fromString("bca");
        Word<Character> w3 = Word.fromString("cb");

        incPcDfa.insert(w1, false);
        incPcDfa.insert(w2, false);

        Assert.assertEquals(incPcDfa.asGraph().size(), 6);

        incPcDfa.insert(Word.epsilon(), false);

        Assert.assertEquals(incPcDfa.asGraph().size(), 1);

        Assert.assertEquals(incPcDfa.lookup(w1).getSecond(), false);
        Assert.assertEquals(incPcDfa.lookup(w2).getSecond(), false);
        Assert.assertEquals(incPcDfa.lookup(w3).getSecond(), false);

        Assert.assertThrows(ConflictException.class, () -> incPcDfa.insert(w3, true));
    }

    @Test
    public void testInvalidSink() {
        final IncrementalConstruction.DFABuilder<Character> incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);

        incPcDfa.insert(Word.fromString("abc"), false);
        incPcDfa.insert(Word.fromString("bca"), true);

        Assert.assertEquals(incPcDfa.asGraph().size(), 7);
        Assert.assertThrows(ConflictException.class, () -> incPcDfa.insert(Word.epsilon(), false));
    }
}
