package org.shanoir.ng.events;

import java.sql.Types;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "events",
		indexes = {
			@Index(name = "i_user_type", columnList = "userId, eventType"),
		}
	)
public class ShanoirEvent extends ShanoirEventLight {

	@JdbcTypeCode(Types.LONGVARCHAR)
	protected String report;


	public ShanoirEvent() {
		// Default empty constructor for json deserializer.
	}

	/**
	 * @return the report
	 */
	public String getReport() {
		return report;
	}

	/**
	 * @param message the report to set
	 */
	public void setReport(String report) {
		this.report = report;
	}

	/** also modifies the current object by setting report to null */
	public ShanoirEventLight toLightEvent() {
		ShanoirEventLight light = (ShanoirEventLight) this;
		light.setHasReport(getReport() != null && !getReport().isEmpty());
		setReport(null);
		return light;
	}
}
