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
import java.util.List;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.common.util.Pair;
import net.automatalib.incremental.IncrementalConstruction;
import net.automatalib.incremental.IntegrationUtil;
import net.automatalib.incremental.IntegrationUtil.ParsedTraces;
import net.automatalib.incremental.dfa.dag.IncrementalPCDFADAGBuilder;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class IncrementalPCDFADAGBuilderTest extends AbstractIncrementalPCDFABuilderTest {

    @Override
    protected <I> IncrementalConstruction.DFABuilder<I> createIncrementalPCDFABuilder(Alphabet<I> alphabet) {
        return new IncrementalPCDFADAGBuilder<>(alphabet);
    }

    @Override
    protected String getDOTResource() {
        return "/dfa/pc_dag.dot";
    }

    /**
     * This tests case validates a set of traces from an external system which exposed an issue in confluence
     * propagation.
     */
    @Test
    public void testIntegrationSPA() throws IOException {
        runIntegration("/spa/dfa_traces.gz");
    }

    /**
     * This tests case validates a set of traces from an external system which exposed an issue in state purging.
     */
    @Test
    public void testIntegrationSBA() throws IOException {
        runIntegration("/sba/dfa_traces.gz");
    }

    private void runIntegration(String path) throws IOException {
        final ParsedTraces<Integer, Boolean> parsedData = IntegrationUtil.parseDFATraces(path);
        final Alphabet<Integer> alphabet = parsedData.alphabet;
        final List<Pair<Word<Integer>, Boolean>> traces = parsedData.traces;

        Assert.assertTrue(IntegrationUtil.isPrefixClosed(traces));

        final IncrementalConstruction.DFABuilder<Integer> cache = createIncrementalPCDFABuilder(alphabet);

        // test insertion without errors
        for (Pair<Word<Integer>, Boolean> trace : traces) {
            cache.insert(trace.getFirst(), trace.getSecond());
        }

        // test caching properties
        for (Pair<Word<Integer>, Boolean> trace : traces) {
            final Word<Integer> word = trace.getFirst();
            final boolean accepted = trace.getSecond();

            Assert.assertEquals(accepted, cache.lookup(word).getSecond());

            // test prefix-closedness
            if (accepted) {
                for (Word<Integer> prefix : word.prefixes(false)) {
                    Assert.assertTrue(cache.lookup(prefix).getSecond());
                }
            }
        }
    }
}
