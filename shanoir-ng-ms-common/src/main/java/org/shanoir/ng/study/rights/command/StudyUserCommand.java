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

package org.shanoir.ng.study.rights.command;

import org.shanoir.ng.study.rights.StudyUserInterface;

public class StudyUserCommand {

	private CommandType type;
	
	private StudyUserInterface studyUser;
	
	private Long studyUserId;
	
	
	/**
	 * Default constructor
	 */
	public StudyUserCommand() {
		this.type = null;
		this.studyUser = null;
		this.studyUserId = null;
	}

	/**
	 * Constructor
	 * 
	 * @param delete
	 * @param id
	 */
	public StudyUserCommand(CommandType type, Long id) {
		this.type = type;
		this.studyUser = null;
		this.studyUserId = id;
	}
	
	/**
	 * Constructor
	 * 
	 * @param type
	 * @param studyUser
	 */
	public StudyUserCommand(CommandType type, StudyUserInterface studyUser) {
		this.type = type;
		this.studyUser = studyUser;
		this.studyUserId = null;
	}
	

	public CommandType getType() {
		return type;
	}

	public void setType(CommandType type) {
		this.type = type;
	}

	public StudyUserInterface getStudyUser() {
		return studyUser;
	}

	public void setStudyUser(StudyUserInterface studyUser) {
		this.studyUser = studyUser;
	}

	public Long getStudyUserId() {
		return studyUserId;
	}

	public void setStudyUserId(Long studyUserId) {
		this.studyUserId = studyUserId;
	}
	
}
