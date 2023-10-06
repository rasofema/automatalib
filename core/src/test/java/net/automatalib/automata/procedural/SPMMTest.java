/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.automata.procedural;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.FastMealy;
import net.automatalib.automata.transducers.impl.FastMealyState;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultProceduralInputAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SPMMTest {

    private final SPMM<?, Character, ?, Character> spmm;
    private final EmptySPMM<Character, Character> emptySpmm;

    public SPMMTest() {
        final Alphabet<Character> smallCallAlphabet = Alphabets.characters('S', 'T');
        final Alphabet<Character> bigCallAlphabet = Alphabets.characters('S', 'U');

        final ProceduralInputAlphabet<Character> smallAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('a', 'c'), smallCallAlphabet, 'R');
        final ProceduralInputAlphabet<Character> bigAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('a', 'c'), bigCallAlphabet, 'R');
        final Character errorOutput = '-';

        final Map<Character, MealyMachine<?, Character, ?, Character>> mealies =
                ImmutableMap.of('S', buildSProcedure(smallAlphabet), 'T', buildTProcedure(smallAlphabet));

        spmm = new StackSPMM<>(bigAlphabet, 'S', '✓', errorOutput, mealies);
        emptySpmm = new EmptySPMM<>(bigAlphabet, errorOutput);
    }

    private static MealyMachine<?, Character, ?, Character> buildSProcedure(ProceduralInputAlphabet<Character> alphabet) {
        final CompactMealy<Character, Character> procedure = new CompactMealy<>(alphabet);

        final int s0 = procedure.addInitialState();
        final int s1 = procedure.addState();
        final int s2 = procedure.addState();
        final int s3 = procedure.addState();
        final int s4 = procedure.addState();
        final int s5 = procedure.addState();
        final int s6 = procedure.addState();

        procedure.addTransition(s0, 'a', s1, 'x');
        procedure.addTransition(s0, 'b', s2, 'y');
        procedure.addTransition(s0, 'T', s5, '✓');
        procedure.addTransition(s0, 'R', s6, '✓');
        procedure.addTransition(s1, 'S', s3, '✓');
        procedure.addTransition(s1, 'R', s6, '✓');
        procedure.addTransition(s2, 'S', s4, '✓');
        procedure.addTransition(s2, 'R', s6, '✓');
        procedure.addTransition(s3, 'a', s5, 'x');
        procedure.addTransition(s4, 'b', s5, 'y');
        procedure.addTransition(s5, 'R', s6, '✓');

        return procedure;
    }

    private static MealyMachine<?, Character, ?, Character> buildTProcedure(ProceduralInputAlphabet<Character> alphabet) {
        final FastMealy<Character, Character> procedure = new FastMealy<>(alphabet);

        final FastMealyState<Character> t0 = procedure.addInitialState();
        final FastMealyState<Character> t1 = procedure.addState();
        final FastMealyState<Character> t2 = procedure.addState();
        final FastMealyState<Character> t3 = procedure.addState();
        final FastMealyState<Character> t4 = procedure.addState();

        procedure.addTransition(t0, 'c', t1, 'z');
        procedure.addTransition(t0, 'S', t3, '✓');
        procedure.addTransition(t1, 'T', t2, '✓');
        procedure.addTransition(t1, 'R', t4, '✓');
        procedure.addTransition(t2, 'c', t3, 'z');
        procedure.addTransition(t3, 'R', t4, '✓');

        return procedure;
    }

    @Test
    public void testSPMM() {
        final Word<Character> i1 = Word.fromCharSequence("SaSTcRRaR");
        final Word<Character> o1 = Word.fromCharSequence("✓x✓✓z✓✓x✓");
        Assert.assertEquals(spmm.computeOutput(i1), o1);

        final Word<Character> i2 = Word.fromCharSequence("SaSbRaR");
        final Word<Character> o2 = Word.fromCharSequence("✓x✓y✓x✓");
        Assert.assertEquals(spmm.computeOutput(i2), o2);

        final Word<Character> i3 = Word.fromCharSequence("SaSbaRcRabc");
        final Word<Character> o3 = Word.fromCharSequence("✓x✓y-------");
        Assert.assertEquals(spmm.computeOutput(i3), o3);

        final Word<Character> i4 = Word.fromCharSequence("SaUcR");
        final Word<Character> o4 = Word.fromCharSequence("✓x---");
        Assert.assertEquals(spmm.computeOutput(i4), o4);

        final Word<Character> i5 = Word.fromCharSequence("TcR");
        final Word<Character> o5 = Word.fromCharSequence("---");
        Assert.assertEquals(spmm.computeOutput(i5), o5);

        final Word<Character> i6 = Word.fromCharSequence("Sd");
        final Word<Character> o6 = Word.fromCharSequence("✓-");
        Assert.assertEquals(spmm.computeOutput(i6), o6);

        final Word<Character> i7 = Word.fromCharSequence("aca");
        final Word<Character> o7 = Word.fromCharSequence("---");
        Assert.assertEquals(spmm.computeOutput(i7), o7);

        final Word<Character> i8 = Word.fromCharSequence("SacTcR");
        final Word<Character> o8 = Word.fromCharSequence("✓x----");
        Assert.assertEquals(spmm.computeOutput(i8), o8);

        final Word<Character> i9 = Word.fromCharSequence("R");
        final Word<Character> o9 = Word.fromCharSequence("-");
        Assert.assertEquals(spmm.computeOutput(i9), o9);

        final Word<Character> i10 = Word.fromCharSequence("STTc");
        final Word<Character> o10 = Word.fromCharSequence("✓✓--");
        Assert.assertEquals(spmm.computeOutput(i10), o10);

        final Word<Character> i11 = Word.fromCharSequence("SaSRR");
        final Word<Character> o11 = Word.fromCharSequence("✓x✓✓-");
        Assert.assertEquals(spmm.computeOutput(i11), o11);

        final Word<Character> i12 = Word.epsilon();
        final Word<Character> o12 = Word.epsilon();
        Assert.assertEquals(spmm.computeOutput(i12), o12);
    }

    @Test
    public void testEmptySPMM() {
        final Word<Character> i1 = Word.fromCharSequence("SaSTcRRaR");
        final Word<Character> o1 = Word.fromCharSequence("---------");
        Assert.assertEquals(emptySpmm.computeOutput(i1), o1);

        final Word<Character> i2 = Word.fromCharSequence("SaSbRaR");
        final Word<Character> o2 = Word.fromCharSequence("-------");
        Assert.assertEquals(emptySpmm.computeOutput(i2), o2);

        final Word<Character> i3 = Word.fromCharSequence("SaSbaRcRabc");
        final Word<Character> o3 = Word.fromCharSequence("-----------");
        Assert.assertEquals(emptySpmm.computeOutput(i3), o3);

        final Word<Character> i4 = Word.fromCharSequence("SaUcR");
        final Word<Character> o4 = Word.fromCharSequence("-----");
        Assert.assertEquals(emptySpmm.computeOutput(i4), o4);

        final Word<Character> i5 = Word.fromCharSequence("TcR");
        final Word<Character> o5 = Word.fromCharSequence("---");
        Assert.assertEquals(emptySpmm.computeOutput(i5), o5);

        final Word<Character> i6 = Word.fromCharSequence("Sd");
        final Word<Character> o6 = Word.fromCharSequence("--");
        Assert.assertEquals(emptySpmm.computeOutput(i6), o6);

        final Word<Character> i7 = Word.fromCharSequence("aca");
        final Word<Character> o7 = Word.fromCharSequence("---");
        Assert.assertEquals(emptySpmm.computeOutput(i7), o7);

        final Word<Character> i8 = Word.fromCharSequence("SacTcR");
        final Word<Character> o8 = Word.fromCharSequence("------");
        Assert.assertEquals(emptySpmm.computeOutput(i8), o8);

        final Word<Character> i9 = Word.fromCharSequence("R");
        final Word<Character> o9 = Word.fromCharSequence("-");
        Assert.assertEquals(emptySpmm.computeOutput(i9), o9);

        final Word<Character> i10 = Word.fromCharSequence("STTc");
        final Word<Character> o10 = Word.fromCharSequence("----");
        Assert.assertEquals(emptySpmm.computeOutput(i10), o10);

        final Word<Character> i11 = Word.fromCharSequence("SaSRR");
        final Word<Character> o11 = Word.fromCharSequence("-----");
        Assert.assertEquals(emptySpmm.computeOutput(i11), o11);

        final Word<Character> i12 = Word.epsilon();
        final Word<Character> o12 = Word.epsilon();
        Assert.assertEquals(spmm.computeOutput(i12), o12);
    }
}
