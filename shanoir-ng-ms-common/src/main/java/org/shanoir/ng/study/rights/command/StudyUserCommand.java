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
