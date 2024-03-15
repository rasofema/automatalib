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
package net.automatalib.incremental.dfa.tree;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.IncrementalConstruction;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.word.Word;

/**
 * Incrementally builds a tree, from a set of positive and negative words. Using {@link #insert(Word, boolean)}, either
 * the set of words definitely in the target language or definitely <i>not</i> in the target language is augmented. The
 * {@link #lookup(Word)} method then returns, for a given word, whether this word is in the set of definitely accepted
 * words ({@link Acceptance#TRUE}), definitely rejected words ({@link Acceptance#FALSE}), or neither
 * ({@link Acceptance#DONT_KNOW}).
 *
 * @param <I>
 *         input symbol class
 */
public class IncrementalDFATreeBuilder<I> extends AbstractAlphabetBasedDFATreeBuilder<I>
        implements IncrementalConstruction.DFABuilder<I>  {


    public IncrementalDFATreeBuilder(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
    }

    @Override
    public void insert(Word<? extends I> word, Boolean acceptance) {
        Node curr = root;

        for (I sym : word) {
            int inputIdx = getInputIndex(sym);
            Node succ = curr.getChild(inputIdx);
            if (succ == null) {
                succ = new Node();
                curr.setChild(inputIdx, getInputAlphabetSize(), succ);
            }
            curr = succ;
        }

        Acceptance acc = curr.getAcceptance();
        Acceptance newWordAcc = Acceptance.fromBoolean(acceptance);
        if (acc == Acceptance.DONT_KNOW) {
            curr.setAcceptance(newWordAcc);
        } else if (acc != newWordAcc) {
            throw new ConflictException(
                    "Conflicting acceptance values for word " + word + ": " + acc + " vs " + newWordAcc);
        }
    }

}
