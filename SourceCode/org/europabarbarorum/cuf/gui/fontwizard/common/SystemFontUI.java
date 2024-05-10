/*
 *  
 *  Copyright (C) 2010, 2011, 2012 The Europa Barbarorum Team
 *  All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of The Europa Barbarorum Team nor the
 *        names of other contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL The Europa Barbarorum Team BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 */

/*
 * SystemFontUI.java
 *
 * Created on Sep 24, 2010, 2:25:12 PM
 */
package org.europabarbarorum.cuf.gui.fontwizard.common;

import java.awt.Dimension;
import org.europabarbarorum.cuf.gui.support.ScrollingSupport;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.europabarbarorum.cuf.gui.support.ComponentState.ComponentModel;
import org.europabarbarorum.cuf.gui.fontwizard.common.SystemFontUI.Model;
import org.europabarbarorum.cuf.gui.support.ComponentState;
import org.europabarbarorum.cuf.gui.support.FileInput;
import org.europabarbarorum.cuf.gui.support.FileType;
import org.europabarbarorum.cuf.gui.support.SettingField;
import org.europabarbarorum.cuf.gui.support.SettingOption.DropDown;
import org.europabarbarorum.cuf.shell.FontStyle;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.support.DefaultOption;

/**
 * A component to hold data for loading a system font.
 * @author Johan Ouwerkerk
 */
public class SystemFontUI extends JPanel implements ComponentState<Model> {

    private final FileInput file;

    /**
     * Creates a new {@link SystemFontUI}.
     * @param shell the context {@link Shell} to use for resolving relative
     * path names.
     */
    public SystemFontUI (Shell shell) {
        file = new FileInput(FileType.Font.derive(false, true),
                             FileInput.Access.Read,
                             shell,
                             this);
        initComponents();
    }

    private DefaultListModel names () {
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final String[] obj = g.getAvailableFontFamilyNames();
        return new DefaultListModel() {
            @Override
            public int getSize () {
                return obj.length;
            }

            @Override
            public Object getElementAt (int index) {
                return obj[index];
            }
        };
    }

    @Override
    public Model createModel () {
        int si = sizeList.getSelectedIndex();
        Object size = sizeList.getModel().getElementAt(si);
        Weight weight = Weight.values()[weightBox.getSelectedIndex()];
        Width width = Width.values()[widthBox.getSelectedIndex()];
        return new Model(file.getValue(),
                         (Integer) size,
                         width.w,
                         weight.w,
                         oblique.isSelected(),
                         kern.isSelected(),
                         underline.isSelected(),
                         strike.isSelected());
    }

    /**
     * The {@link ComponentModel} for the {@link SystemFontUI} component.
     */
    public static class Model implements ComponentModel<SystemFontUI> {

        private final String family;
        private final int size;
        private final float width, weight;
        private final boolean kern, strike, underline, italic;

        private Model (String family,
                       int size,
                       float width,
                       float weight,
                       boolean italic,
                       boolean kern,
                       boolean underline,
                       boolean strike) {
            this.family = family;
            this.size = size;
            this.weight = weight;
            this.width = width;
            this.italic = italic;
            this.kern = kern;
            this.underline = underline;
            this.strike = strike;
        }

        /**
         * Convert the font data into a {@link FontStyle} object.
         * @return the {@link FontStyle} represented by the data in this {@link Model}.
         */
        public FontStyle getStyle () {
            FontStyle s = new FontStyle(family, size);
            if (strike) {
                s.strikethrough();
            }
            if (underline) {
                s.underline();
            }
            if (italic) {
                s.oblique();
            }
            if (kern) {
                s.requestKerning();
            }
            s.weight(weight);
            s.width(width);
            return s;
        }

        @Override
        public void populate (SystemFontUI ui) {
            ui.sizeList.setSelectedIndex(size - 1);
            ui.fontList.setSelectedValue(family, true);
            ui.file.getTextField().setValue(family);
            ui.widthBox.setSelectedIndex(Width.forWidth(width).ordinal());
            ui.weightBox.setSelectedIndex(Weight.forWeight(weight).ordinal());
            ui.oblique.setSelected(italic);
            ui.kern.setSelected(kern);
            ui.strike.setSelected(strike);
            ui.underline.setSelected(underline);
        }
    }

    private DefaultListModel sizes () {
        return new DefaultListModel() {

            @Override
            public int getSize () {
                return 0xFF;
            }

            @Override
            public Object getElementAt (int index) {
                return index + 1;
            }
        };
    }

    /**
     * Font (stroke) weight options.
     */
    public static enum Weight implements DefaultOption {

        /**
         * Extra light weight.
         */
        ExtraLight(TextAttribute.WEIGHT_EXTRA_LIGHT),
        /**
         * Standard light weight.
         */
        Light(TextAttribute.WEIGHT_LIGHT),
        /**
         * Moderately lighter weight than regular.
         */
        DemiLight(TextAttribute.WEIGHT_DEMILIGHT),
        /**
         * Regular weight.
         */
        Regular(TextAttribute.WEIGHT_REGULAR),
        /**
         * Moderately heavier weight than regular.
         */
        SemiBold(TextAttribute.WEIGHT_SEMIBOLD),
        /**
         * Moderately lighter weight than bold.
         */
        DemiBold(TextAttribute.WEIGHT_DEMIBOLD),
        /**
         * Standard bold weight.
         */
        Bold(TextAttribute.WEIGHT_BOLD),
        /**
         * Moderately heavier weight than bold.
         */
        Heavy(TextAttribute.WEIGHT_HEAVY),
        /**
         * Extra heavy weight.
         */
        ExtraBold(TextAttribute.WEIGHT_EXTRABOLD),
        /**
         * Heaviest predefined weight.
         */
        UltraBold(TextAttribute.WEIGHT_ULTRABOLD);

        private Weight (float w) {
            this.w = w;
        }
        private final float w;

        @Override
        public DefaultOption defaultOption () {
            return Regular;
        }

        private static Weight forWeight (float w) {
            for (Weight weight : Weight.values()) {
                if (w == weight.w) {
                    return weight;
                }
            }
            return null;
        }
    }

    /**
     * Font (stroke) width options.
     */
    public static enum Width implements DefaultOption {

        /**
         * Condensed width
         */
        Condensed(TextAttribute.WIDTH_CONDENSED),
        /**
         * Moderately condensed width
         */
        SemiCondensed(TextAttribute.WIDTH_SEMI_CONDENSED),
        /**
         * Regular width
         */
        Regular(TextAttribute.WIDTH_REGULAR),
        /**
         * Moderately extended width
         */
        SemiExtended(TextAttribute.WIDTH_SEMI_EXTENDED),
        /**
         * Extended width
         */
        Extended(TextAttribute.WIDTH_EXTENDED);

        private Width (float w) {
            this.w = w;
        }
        private final float w;

        @Override
        public DefaultOption defaultOption () {
            return Regular;
        }

        private static Width forWidth (float w) {
            for (Width width : Width.values()) {
                if (w == width.w) {
                    return width;
                }
            }
            return null;
        }
    }

    /**
     * Create a new {@link SystemFontUI} for GUI builder purposes.
     * Makes this class a valid JavaBean.
     */
    public SystemFontUI () {
        this(null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        JButton loadButton = file.getButton();
        SettingField fontField = file.getTextField();
        JScrollPane fontScroll = new ScrollingSupport();
        fontList = new JList();
        JScrollPane sizeScroll = new ScrollingSupport();
        sizeList = new JList();
        JPanel options = new JPanel();
        oblique = new JCheckBox();
        underline = new JCheckBox();
        strike = new JCheckBox();
        widthBox = new DropDown(Width.values(), SystemFontUI.class, Width.class);
        JLabel weight = new JLabel();
        JLabel width = new JLabel();
        kern = new JCheckBox();
        weightBox = new DropDown(Weight.values(), SystemFontUI.class, Weight.class);

        setName("Form"); // NOI18N

        ResourceBundle bundle = ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/fontwizard/common/SystemFontUIComponent"); // NOI18N
        loadButton.setText(bundle.getString("SystemFontUI.loadButton.text")); // NOI18N
        loadButton.setName("loadButton"); // NOI18N

        fontField.setText(bundle.getString("SystemFontUI.fontField.text")); // NOI18N
        fontField.setName("fontField"); // NOI18N

        fontScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(bundle.getString("SystemFontUI.fontScroll.title")), fontScroll.getBorder())); // NOI18N
        fontScroll.setMinimumSize(new Dimension(178, 245));
        fontScroll.setName("fontScroll"); // NOI18N

        fontList.setModel(names());
        fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fontList.setName("fontList"); // NOI18N
        fontList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                fontListValueChanged(evt);
            }
        });
        fontScroll.setViewportView(fontList);

        sizeScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(bundle.getString("SystemFontUI.sizeScroll.title")), sizeScroll.getBorder())); // NOI18N
        sizeScroll.setName("sizeScroll"); // NOI18N

        sizeList.setModel(sizes());
        sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sizeList.setName("sizeList"); // NOI18N
        sizeScroll.setViewportView(sizeList);

        options.setBorder(BorderFactory.createTitledBorder(bundle.getString("SystemFontUI.options.title"))); // NOI18N
        options.setName("options"); // NOI18N

        oblique.setText(bundle.getString("SystemFontUI.oblique.text")); // NOI18N
        oblique.setHorizontalTextPosition(SwingConstants.LEADING);
        oblique.setName("oblique"); // NOI18N

        underline.setText(bundle.getString("SystemFontUI.underline.text")); // NOI18N
        underline.setHorizontalTextPosition(SwingConstants.LEADING);
        underline.setName("underline"); // NOI18N

        strike.setText(bundle.getString("SystemFontUI.strike.text")); // NOI18N
        strike.setHorizontalTextPosition(SwingConstants.LEADING);
        strike.setName("strike"); // NOI18N

        widthBox.setName("widthBox"); // NOI18N

        weight.setText(bundle.getString("SystemFontUI.weight.text")); // NOI18N
        weight.setName("weight"); // NOI18N

        width.setText(bundle.getString("SystemFontUI.width.text")); // NOI18N
        width.setName("width"); // NOI18N

        kern.setText(bundle.getString("SystemFontUI.kern.text")); // NOI18N
        kern.setHorizontalTextPosition(SwingConstants.LEADING);
        kern.setName("kern"); // NOI18N

        weightBox.setName("weightBox"); // NOI18N

        GroupLayout optionsLayout = new GroupLayout(options);
        options.setLayout(optionsLayout);
        optionsLayout.setHorizontalGroup(
            optionsLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(optionsLayout.createSequentialGroup()
                .addContainerGap(29, Short.MAX_VALUE)
                .addGroup(optionsLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(strike, Alignment.TRAILING)
                    .addComponent(underline, Alignment.TRAILING)
                    .addComponent(oblique, Alignment.TRAILING)
                    .addGroup(Alignment.TRAILING, optionsLayout.createSequentialGroup()
                        .addGroup(optionsLayout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(width)
                            .addComponent(weight))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(optionsLayout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(weightBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(widthBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .addComponent(kern, Alignment.TRAILING))
                .addContainerGap())
        );

        optionsLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {weightBox, widthBox});

        optionsLayout.setVerticalGroup(
            optionsLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(optionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(weightBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(weight))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(optionsLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(widthBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(width))
                .addGap(4, 4, 4)
                .addComponent(oblique)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(underline)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(strike)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(kern)
                .addContainerGap(58, Short.MAX_VALUE))
        );

        optionsLayout.linkSize(SwingConstants.VERTICAL, new Component[] {weightBox, widthBox});

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fontScroll, GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(options, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(sizeScroll, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fontField, GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(loadButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(loadButton)
                    .addComponent(fontField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(fontScroll, GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                    .addComponent(sizeScroll, GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                    .addComponent(options, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fontListValueChanged (ListSelectionEvent evt) {//GEN-FIRST:event_fontListValueChanged
        if (!evt.getValueIsAdjusting()) {
            String s = fontList.getSelectedIndex() == -1
                    ? null
                    : fontList.getSelectedValue().toString();
            file.getTextField().setValue(s);
        }
    }//GEN-LAST:event_fontListValueChanged

    private boolean nameFromList () {
        Object selected = fontList.getSelectedValue();
        String value = file.getValue();
        return selected != null && value != null && value.equals(selected.
                toString());
    }

    @Override
    public boolean checkUI () {
        // check if the name is from the font list or failing that if it is a
        // valid file.
        boolean n = nameFromList() || file.check(),
                s = sizeList.getSelectedIndex() != -1; // size is required!
        return n && s;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JList fontList;
    private JCheckBox kern;
    private JCheckBox oblique;
    private JList sizeList;
    private JCheckBox strike;
    private JCheckBox underline;
    protected JComboBox weightBox;
    protected JComboBox widthBox;
    // End of variables declaration//GEN-END:variables
}
