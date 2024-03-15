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
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.graph.UniversalAutomatonGraphView;
import net.automatalib.automaton.visualization.FSAVisualizationHelper;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.common.util.mapping.MapMapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.graph.Graph;
import net.automatalib.incremental.dfa.AbstractVisualizationHelper;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.automaton.fsa.DFA.DFAGraphView;
import net.automatalib.visualization.VisualizationHelper;

import java.util.Collection;

abstract class AbstractAlphabetBasedDFATreeBuilder<I> extends AbstractDFATreeBuilder<I>
        implements InputAlphabetHolder<I> {

    private final Alphabet<I> inputAlphabet;
    private int alphabetSize;

    AbstractAlphabetBasedDFATreeBuilder(Alphabet<I> inputAlphabet) {
        super(new Node(), inputAlphabet);
        this.inputAlphabet = inputAlphabet;
        this.alphabetSize = inputAlphabet.size();
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        if (!inputAlphabet.containsSymbol(symbol)) {
            inputAlphabet.asGrowingAlphabetOrThrowException().addSymbol(symbol);
        }

        final int newAlphabetSize = inputAlphabet.size();
        // even if the symbol was already in the alphabet, we need to make sure to be able to store the new symbol
        if (alphabetSize < newAlphabetSize) {
            ensureInputCapacity(root, alphabetSize, newAlphabetSize);
            alphabetSize = newAlphabetSize;
        }
    }

    private void ensureInputCapacity(Node node, int oldAlphabetSize, int newAlphabetSize) {
        node.ensureInputCapacity(newAlphabetSize);
        for (int i = 0; i < oldAlphabetSize; i++) {
            final Node child = node.getChild(i);
            if (child != null) {
                ensureInputCapacity(child, oldAlphabetSize, newAlphabetSize);
            }
        }
    }

    @Override
    Node createNode() {
        return new Node();
    }

    @Override
    Node insertNode(Node parent, I symIdx, Boolean accept) {
        Node succ = new Node(Acceptance.fromBoolean(accept));
        parent.setChild(inputAlphabet.getSymbolIndex(symIdx), alphabetSize, succ);
        return succ;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return inputAlphabet;
    }

    public int getInputAlphabetSize() {
        return inputAlphabet.size();
    }

    public int getInputIndex(I sym) {
        return inputAlphabet.getSymbolIndex(sym);
    }

    @Override
    public Graph<Node, ?> asGraph() {
        return new UniversalAutomatonGraphView<Node, I, Node, Acceptance, Void, TransitionSystemViewAccept>(new TransitionSystemViewAccept(),
                inputAlphabet) {

            @Override
            public VisualizationHelper<Node, TransitionEdge<I, Node>> getVisualizationHelper() {
                return new AbstractVisualizationHelper<Node, I, Node, TransitionSystemViewAccept>(automaton) {

                    @Override
                    public Acceptance getAcceptance(Node node) {
                        return node.getAcceptance();
                    }
                };
            }
        };
    }
}
