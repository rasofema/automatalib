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

import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;

/**
 * Basic interface for incremental automata constructions. An incremental
 * automaton construction creates an (acyclic)
 * automaton by iterated insertion of non-conflicting example words.
 *
 * @param <A>
 *            the automaton model which is constructed
 * @param <I>
 *            input symbol class
 * @param <D>
 *            output domain class
 */
public interface IncrementalConstruction<M extends Output<I, D>, I, D> extends Construction<M, I, D> {

    void insert(Word<? extends I> input, D output) throws ConflictException;

    interface MealyBuilder<I, O> extends IncrementalConstruction<MealyMachine<?, I, ?, O>, I, Word<O>> {
    }

    interface DFABuilder<I> extends IncrementalConstruction<DFA<?, I>, I, Boolean> {
        /**
         * Inserts a new word into the automaton. This is a convenience method
         * equivalent to invoking {@code insert(word,
         * true)}.
         *
         * @param word
         *             the word to insert
         *
         * @throws ConflictException
         *                           if the newly provided information conflicts with
         *                           existing information
         * @see #insert(Word, boolean)
         */
        default void insert(Word<? extends I> word) {
            insert(word, true);
        }

    }

    interface MooreBuilder<I, O> extends IncrementalConstruction<MooreMachine<?, I, ?, O>, I, Word<O>> {
    }
}
