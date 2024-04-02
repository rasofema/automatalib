package net.automatalib.incremental;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public interface AdaptiveConstruction<M extends Output<I, D>, I, D> extends Construction<M, I, D> {

    /**
     * Incorporates a pair of input/output words into the stored information.
     *
     * @param inputWord
     *                   the input word
     * @param outputWord
     *                   the corresponding output word
     *
     * @return {@code true} if the inserted output word has overridden existing
     *         information to handle a conflict, {@code false} otherwise.
     */
    boolean insert(Word<? extends I> input, D output);

    default boolean insertFromUser(Word<? extends I> input, D output) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the oldest, non-overridden input that has been introduced and
     * persisted.
     *
     * @return the {@code Word} representing the oldest stored input, {@code null}
     *         if the cache is empty.
     */
    @Nullable
    Word<I> getOldestInput();

    interface MealyBuilder<I, O>
            extends AdaptiveConstruction<MealyMachine<?, I, ?, O>, I, Word<O>>, Construction.MealyBuilder<I, O> {
    }

    interface DFABuilder<I> extends AdaptiveConstruction<DFA<?, I>, I, Boolean>, Construction.DFABuilder<I> {
    }

}
