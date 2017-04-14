package org.shanoir.ng.subject;

import javax.persistence.Transient;

public interface IDisplayableObject {
	
	/**
	 * Return the string to be displayed.
	 * 
	 * @return the display string
	 */
	@Transient
	String getDisplayString();

}
