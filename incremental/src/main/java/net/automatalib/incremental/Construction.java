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
package net.automatalib.incremental;

import java.util.Collection;

import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.graph.Graph;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.UniversalDTS;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.ts.output.MooreTransitionSystem;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Basic interface for incremental and adaptive automata constructions. An
 * automaton construction creates an (acyclic)
 * automaton by iterated insertion of example words.
 *
 * @param <A>
 *            the automaton model which is constructed
 * @param <I>
 *            input symbol class
 * @param <D>
 *            output domain class
 */
public interface Construction<A extends Output<I, D>, I, D> extends SupportsGrowingAlphabet<I> {

    /**
     * Looks up the output value for a given word.
     *
     * @param input
     *              the word
     *
     * @return whether the the query information found, and whether it is complete,
     *         iff so, {@code lookup(w).first().getInput() == w}.
     */
    Pair<Boolean, D> lookup(Word<? extends I> input);

    /**
     * Looks up the output value for a given word.
     *
     * @param input
     *              the word
     *
     * @return the query information found.
     */
    default D directLookup(Word<? extends I> input) {
        return lookup(input).getSecond();
    }

    /**
     * Checks the current state of the construction against a given target model,
     * and returns a word exposing a
     * difference if there is one.
     *
     * @param target
     *                      the target automaton model
     * @param inputs
     *                      the set of input symbols to consider
     * @param omitUndefined
     *                      if this is set to {@code true}, then undefined
     *                      transitions in the {@code target} model will be
     *                      interpreted as "unspecified/don't know" and omitted in
     *                      the equivalence test. Otherwise, they will be
     *                      interpreted in the usual manner (e.g., non-accepting
     *                      sink in case of DFAs).
     *
     * @return a separating word, or {@code null} if no difference could be found.
     */
    @Nullable
    Word<I> findSeparatingWord(A target, Collection<? extends I> inputs, boolean omitUndefined);

    /**
     * Checks whether this class has definitive information about a given word.
     *
     * @param word
     *             the word
     *
     * @return {@code true} if this class has definitive information about the word,
     *         {@code false} otherwise.
     */
    default boolean hasDefinitiveInformation(Word<? extends I> word) {
        return lookup(word).getFirst();
    }

    /**
     * Retrieves a <i>graph view</i> of the current state of the construction. The
     * graph model should be backed by the
     * construction, i.e., subsequent changes will be reflected in the graph model.
     *
     * @return a graph view on the current state of the construction
     */
    Graph<?, ?> asGraph();

    /**
     * Retrieves a <i>transition system view</i> of the current state of the
     * construction. The transition system model
     * should be backed by the construction, i.e., subsequent changes will be
     * reflected in the transition system.
     *
     * @return a transition system view on the current state of the construction
     */
    DeterministicTransitionSystem<?, I, ?> asTransitionSystem();

    interface MealyBuilder<I, O>
            extends Construction<MealyMachine<?, I, ?, O>, I, Word<O>> {

        @Override
        MealyTransitionSystem<?, I, ?, O> asTransitionSystem();
    }

    interface DFABuilder<I> extends Construction<DFA<?, I>, I, Boolean> {

        @Override
        UniversalDTS<?, I, ?, Acceptance, Void> asTransitionSystem();
    }

    interface MooreBuilder<I, O> extends Construction<MooreMachine<?, I, ?, O>, I, Word<O>> {

        @Override
        MooreTransitionSystem<?, I, ?, O> asTransitionSystem();
    }
}
