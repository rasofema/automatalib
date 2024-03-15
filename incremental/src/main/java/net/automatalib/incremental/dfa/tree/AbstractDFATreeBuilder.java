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
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.common.util.mapping.MapMapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.incremental.Construction;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.ts.UniversalDTS;
import net.automatalib.ts.acceptor.DeterministicAcceptorTS;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

abstract class AbstractDFATreeBuilder<I> implements Construction.DFABuilder<I> {

    final Node root;
    final Alphabet<I> inputAlphabet;

    AbstractDFATreeBuilder(Node root, Alphabet<I> inputAlphabet) {
        this.root = root;
        this.inputAlphabet = inputAlphabet;
    }

    @Override
    public @Nullable Word<I> findSeparatingWord(DFA<?, I> target,
                                                Collection<? extends I> inputs,
                                                boolean omitUndefined) {
        return doFindSeparatingWord(target, inputs, omitUndefined);
    }

    <S> @Nullable Word<I> doFindSeparatingWord(DFA<S, I> target,
                                               Collection<? extends I> inputs,
                                               boolean omitUndefined) {
        S automatonInit = target.getInitialState();

        if (automatonInit == null) {
            return omitUndefined ? null : Word.epsilon();
        }

        if (root.getAcceptance().conflicts(target.isAccepting(automatonInit))) {
            return Word.epsilon();
        }

        // incomingInput can be null here, because we will always skip the bottom stack element below
        @SuppressWarnings("nullness")
        AdaptiveDFATreeBuilder.Record<@Nullable S, I> init = new AdaptiveDFATreeBuilder.Record<>(automatonInit, root, null, inputs.iterator());

        Deque<AdaptiveDFATreeBuilder.Record<@Nullable S, I>> dfsStack = new ArrayDeque<>();
        dfsStack.push(init);

        while (!dfsStack.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            AdaptiveDFATreeBuilder.@NonNull Record<@Nullable S, I> rec = dfsStack.peek();
            if (!rec.inputIt.hasNext()) {
                dfsStack.pop();
                continue;
            }
            I input = rec.inputIt.next();
            int inputIdx = inputAlphabet.getSymbolIndex(input);

            Node succ = rec.treeNode.getChild(inputIdx);
            if (succ == null) {
                continue;
            }

            @Nullable S state = rec.automatonState;
            @Nullable S automatonSucc = state == null ? null : target.getTransition(state, input);
            if (automatonSucc == null && omitUndefined) {
                continue;
            }

            boolean succAcc = automatonSucc != null && target.isAccepting(automatonSucc);

            if (succ.getAcceptance().conflicts(succAcc)) {
                WordBuilder<I> wb = new WordBuilder<>(dfsStack.size());
                wb.append(input);

                dfsStack.pop();
                while (!dfsStack.isEmpty()) {
                    wb.append(rec.incomingInput);
                    rec = dfsStack.pop();
                }
                return wb.reverse().toWord();
            }

            dfsStack.push(new AdaptiveDFATreeBuilder.Record<>(automatonSucc, succ, input, inputs.iterator()));
        }

        return null;
    }

    public Pair<Boolean, Boolean> lookup(Word<? extends I> inputWord) {
        Node curr = root;

        for (I sym : inputWord) {
            int symIdx = inputAlphabet.getSymbolIndex(sym);
            Node succ = curr.getChild(symIdx);
            if (succ == null) {
                return Pair.of(false, null);
            }
            curr = succ;
        }
        Boolean out = curr.getAcceptance() == Acceptance.DONT_KNOW ? null : curr.getAcceptance().toBoolean();
        return Pair.of(out != null, out);
    }

    @Override
    public UniversalDTS<?, I, ?, Acceptance, Void> asTransitionSystem() {
        return new TransitionSystemViewAccept();
    }

    abstract Node createNode();

    abstract Node insertNode(Node parent, I symIdx, Boolean accept);

    static final class Record<S, I> {

        public final S automatonState;
        public final Node treeNode;
        public final I incomingInput;
        public final Iterator<? extends I> inputIt;

        Record(S automatonState, Node treeNode, I incomingInput, Iterator<? extends I> inputIt) {
            this.automatonState = automatonState;
            this.treeNode = treeNode;
            this.incomingInput = incomingInput;
            this.inputIt = inputIt;
        }
    }

    class TransitionSystemViewAccept implements UniversalDTS<Node, I, Node, Acceptance, Void>,
                                                UniversalAutomaton<Node, I, Node, Acceptance, Void> {

        @Override
        public Node getSuccessor(Node transition) {
            return transition;
        }

        @Override
        public @Nullable Node getTransition(Node state, I input) {
            int inputIdx = inputAlphabet.getSymbolIndex(input);
            return state.getChild(inputIdx);
        }

        @Override
        public Node getInitialState() {
            return root;
        }

        @Override
        public Acceptance getStateProperty(Node state) {
            return state.getAcceptance();
        }

        @Override
        public Void getTransitionProperty(Node transition) {
            return null;
        }

        @Override
        public Collection<Node> getStates() {
            return IteratorUtil.list(TSTraversal.breadthFirstIterator(this, inputAlphabet));
        }

        /*
         * We need to override the default MooreMachine mapping, because its StateIDStaticMapping class requires our
         * nodeIDs, which requires our states, which requires our nodeIDs, which requires ... infinite loop!
         */
        @Override
        public <V> MutableMapping<Node, V> createStaticStateMapping() {
            return new MapMapping<>();
        }
    }

    class TransitionSystemViewBool implements UniversalDTS<Node, I, Node, Boolean, Void>,
                                            UniversalAutomaton<Node, I, Node, Boolean, Void> {

        @Override
        public Node getSuccessor(Node transition) {
            return transition;
        }

        @Override
        public @Nullable Node getTransition(Node state, I input) {
            int inputIdx = inputAlphabet.getSymbolIndex(input);
            return state.getChild(inputIdx);
        }

        @Override
        public Node getInitialState() {
            return root;
        }

        @Override
        public Boolean getStateProperty(Node state) {
            return state.getAcceptance().toBoolean();
        }

        @Override
        public Void getTransitionProperty(Node transition) {
            return null;
        }

        @Override
        public Collection<Node> getStates() {
            return IteratorUtil.list(TSTraversal.breadthFirstIterator(this, inputAlphabet));
        }

        /*
         * We need to override the default MooreMachine mapping, because its StateIDStaticMapping class requires our
         * nodeIDs, which requires our states, which requires our nodeIDs, which requires ... infinite loop!
         */
        @Override
        public <V> MutableMapping<Node, V> createStaticStateMapping() {
            return new MapMapping<>();
        }
    }
}
