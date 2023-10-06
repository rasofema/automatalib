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
package net.automatalib.incremental.mealy;

import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A variation of the {@link IncrementalMealyBuilder} interface that allows one to override previously inserted traces.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public interface AdaptiveMealyBuilder<I, O> extends MealyBuilder<I, O> {

    /**
     * Incorporates a pair of input/output words into the stored information.
     *
     * @param inputWord
     *         the input word
     * @param outputWord
     *         the corresponding output word
     *
     * @return {@code true} if the inserted output word has overridden existing information, {@code false} otherwise.
     */
    boolean insert(Word<? extends I> inputWord, Word<? extends O> outputWord);

    /**
     * Returns the oldest, non-overridden input that has been introduced and persisted.
     *
     * @return the {@code Word} representing the oldest stored input, {@code null} if the cache is empty.
     */
    @Nullable Word<I> getOldestInput();

}
