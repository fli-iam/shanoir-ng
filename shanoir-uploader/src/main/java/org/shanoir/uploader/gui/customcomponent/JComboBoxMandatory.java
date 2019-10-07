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
