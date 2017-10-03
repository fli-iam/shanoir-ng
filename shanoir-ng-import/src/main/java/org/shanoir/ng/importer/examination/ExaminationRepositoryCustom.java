package org.shanoir.ng.importer.examination;

import java.util.List;

/**
 * Custom repository for Examination.
 * 
 * @author ifakhfakh
 *
 */
public interface ExaminationRepositoryCustom {

	/**
	 * Find templates by field value.
	 * 
	 * @param fieldName
	 *            searched field name.
	 * @param value
	 *            value.
	 * @return list of templates.
	 */
	List<Examination> findBy(String fieldName, Object value);

}
