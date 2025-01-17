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

import net.automatalib.alphabet.Alphabet;
import net.automatalib.incremental.AdaptiveConstruction;
import net.automatalib.incremental.CexOrigin;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.util.graph.traversal.GraphTraversal;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class AdaptiveDFATreeBuilder<I> extends AbstractAlphabetBasedDFATreeBuilder<I>
        implements AdaptiveConstruction.DFABuilder<I> {
    private final Map<Node, Word<I>> nodeToQuery;
    public AdaptiveDFATreeBuilder(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
        this.nodeToQuery = new LinkedHashMap<>();
    }

    private boolean insert(Word<? extends I> word, Boolean acceptance, CexOrigin origin) {
        Node curr = root;
        boolean hasOverwritten = false;

        for (I sym : word) {
            int inputIdx = getInputIndex(sym);
            Node succ = curr.getChild(inputIdx);
            if (succ == null) {
                succ = new Node();
                curr.setChild(inputIdx, getInputAlphabetSize(), succ);
            }
            curr = succ;
        }

        assert curr != null;

        Acceptance acc = curr.getAcceptance();
        Acceptance newWordAcc = Acceptance.fromBoolean(acceptance);
        if (acc == Acceptance.DONT_KNOW) {
            curr.setAcceptance(newWordAcc);
            curr.setOrigin(origin);
//      Only update if from same origin or, otherwise, origin of curr is not user
        } else if (acc != newWordAcc && (origin == curr.getOrigin() || curr.getOrigin() != CexOrigin.USER)) {
            hasOverwritten = true;
            curr.setAcceptance(newWordAcc);
            curr.setOrigin(origin);
        }

        // Make sure it uses the new ages.
        nodeToQuery.remove(curr);
        nodeToQuery.put(curr, Word.upcast(word));

        return hasOverwritten;
    }


    @Override
    public boolean insert(Word<? extends I> word, Boolean acceptance) {
        return insert(word, acceptance, CexOrigin.SUL);
    }

    @Override
    public boolean insertFromUser(Word<? extends I> word, Boolean acceptance) {
        return insert(word, acceptance, CexOrigin.USER);
    }




    private void removeQueries(Node node) {
        GraphTraversal.breadthFirstIterator(this.asGraph(), Collections.singleton(node))
                .forEachRemaining(nodeToQuery::remove);
    }

    @Override
    public @Nullable Word<I> getOldestInput() {
        final Iterator<Word<I>> iter = nodeToQuery.values().iterator();
        return iter.hasNext() ? iter.next() : null;
    }
}
