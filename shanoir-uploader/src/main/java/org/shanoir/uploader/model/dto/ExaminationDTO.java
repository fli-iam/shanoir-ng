package org.shanoir.uploader.model.dto;

import javax.xml.datatype.XMLGregorianCalendar;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.utils.Util;

public class ExaminationDTO {

	private Long id;

	private XMLGregorianCalendar examinationDate;

	private String comment;

	public ExaminationDTO(Long id, XMLGregorianCalendar examinationDate, String comment) {
		super();
		this.id = id;
		this.examinationDate = examinationDate;
		this.comment = comment;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public XMLGregorianCalendar getExaminationDate() {
		return examinationDate;
	}

	public void setExaminationDate(XMLGregorianCalendar examinationDate) {
		this.examinationDate = examinationDate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String toString() {
		final String examinationDate = ShUpConfig.formatter.format(Util.toDate(this.getExaminationDate()));
		return examinationDate + ", " + this.getComment() + " (id = " + this.getId() + ")";
	}

}
