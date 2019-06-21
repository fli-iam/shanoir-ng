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

<<<<<<< HEAD:shanoir-ng-users/src/main/java/org/shanoir/ng/accountrequest/model/AccountRequestInfo.java
package org.shanoir.ng.accountrequest.model;
=======
package org.shanoir.ng.accountrequest;
>>>>>>> upstream/develop:shanoir-ng-users/src/main/java/org/shanoir/ng/accountrequest/AccountRequestInfo.java

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * User account request information.
 * 
 * @author msimon
 *
 */
@Entity
public class AccountRequestInfo extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -1062233564645766489L;

	@NotNull
	private String contact;

	@NotNull
	private String function;

	@NotNull
	private String institution;

	@NotNull
	private String service;

	@NotNull
	private String study;

	@NotNull
	private String work;

	/**
	 * @return the contact
	 */
	public String getContact() {
		return contact;
	}

	/**
	 * @param contact the contact to set
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}

	/**
	 * @return the function
	 */
	public String getFunction() {
		return function;
	}

	/**
	 * @param function the function to set
	 */
	public void setFunction(String function) {
		this.function = function;
	}

	/**
	 * @return the institution
	 */
	public String getInstitution() {
		return institution;
	}

	/**
	 * @param institution the institution to set
	 */
	public void setInstitution(String institution) {
		this.institution = institution;
	}

	/**
	 * @return the service
	 */
	public String getService() {
		return service;
	}

	/**
	 * @param service the service to set
	 */
	public void setService(String service) {
		this.service = service;
	}

	/**
	 * @return the study
	 */
	public String getStudy() {
		return study;
	}

	/**
	 * @param study the study to set
	 */
	public void setStudy(String study) {
		this.study = study;
	}

	/**
	 * @return the work
	 */
	public String getWork() {
		return work;
	}

	/**
	 * @param work the work to set
	 */
	public void setWork(String work) {
		this.work = work;
	}

}
