/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.uploader.gui.customcomponent;

import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

public class JComboBoxMandatory<E> extends JComboBox {

    public JComboBoxMandatory() {
        super();
    }

    public JComboBoxMandatory(ComboBoxModel aModel) {
        super(aModel);
    }

    public JComboBoxMandatory(Object[] items) {
        super(items);
    }

    public JComboBoxMandatory(Vector items) {
        super(items);
    }

    private boolean valueSet;

    public boolean isValueSet() {
        return valueSet;
    }

    public void setValueSet(boolean valueSet) {
        this.valueSet = valueSet;
    }

}
