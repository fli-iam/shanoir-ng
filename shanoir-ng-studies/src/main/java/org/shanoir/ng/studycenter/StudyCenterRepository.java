package org.shanoir.ng.studycenter;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for relations between a study and a center.
 *
 * @author msimon
 */
public interface StudyCenterRepository extends CrudRepository<StudyCenter, Long> {

}
