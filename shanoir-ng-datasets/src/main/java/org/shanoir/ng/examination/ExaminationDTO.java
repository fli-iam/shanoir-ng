/**
 * 
 */
package org.shanoir.ng.examination;

import java.util.Date;

/**
 * @author yyao
 *
 */
public class ExaminationDTO {
	
	private Long id;

	private String comment;
	
	private Date examinationDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getExaminationDate() {
		return examinationDate;
	}

	public void setExaminationDate(Date examinationDate) {
		this.examinationDate = examinationDate;
	}
	
	
}
