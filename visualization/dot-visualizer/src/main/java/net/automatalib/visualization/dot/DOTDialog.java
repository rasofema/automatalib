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
package net.automatalib.visualization.dot;

import java.awt.Dialog;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;

class DOTDialog extends JDialog {

    DOTDialog(String dot, boolean modal) throws IOException {
        super((Dialog) null, modal);

        final PlottedGraph pg = new PlottedGraph("Graph", dot);
        final DOTImageComponent cmp = new DOTImageComponent();
        cmp.setData(pg);

        final JScrollPane scrollPane = new JScrollPane(cmp);
        setContentPane(scrollPane);
        setPreferredSize(new Dimension(DOTUtil.DEFAULT_WIDTH, DOTUtil.DEFAULT_HEIGHT));

        final JMenu menu = new JMenu("File");
        menu.add(cmp.getSavePngAction());
        menu.add(cmp.getSaveDotAction());
        menu.addSeparator();
        menu.add(DOTUtil.getCloseAction(this));

        final JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        setJMenuBar(menuBar);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addKeyListener(DOTUtil.closeOnEscapeAdapter(this));

        pack();
        setVisible(true);
    }
}
