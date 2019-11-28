package org.shanoir.uploader.gui.customcomponent;

import javax.swing.JTextField;

public class JTextFieldMandatory extends JTextField {

	private boolean valueSet;

	public boolean isValueSet() {
		return valueSet;
	}

	public void setValueSet(boolean valueSet) {
		this.valueSet = valueSet;
	}
	
	
}
