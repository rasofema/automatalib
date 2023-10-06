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
package net.automatalib.modelcheckers.m3c.formula;

import java.io.IOException;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstract super-class for modal (sub-) formulas.
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 */
public abstract class AbstractModalFormulaNode<L, AP> extends AbstractUnaryFormulaNode<L, AP> {

    private final @Nullable L action;

    public AbstractModalFormulaNode(@Nullable L action, FormulaNode<L, AP> node) {
        super(node);
        this.action = action;
    }

    public @Nullable L getAction() {
        return action;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(action);
        return result;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!super.equals(o)) {
            return false;
        }

        final AbstractModalFormulaNode<?, ?> that = (AbstractModalFormulaNode<?, ?>) o;

        return Objects.equals(this.action, that.action);
    }

    protected void printMuCalcNode(Appendable a, char leftModalitySymbol, char rightModalitySymbol) throws IOException {
        a.append('(');
        a.append(leftModalitySymbol);

        if (action != null) {
            a.append(action.toString());
        }

        a.append(rightModalitySymbol);
        a.append(' ');
        getChild().print(a);
        a.append(')');
    }

}
