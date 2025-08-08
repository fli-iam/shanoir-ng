package org.shanoir.ng.subjectstudy.repository;

import jakarta.transaction.Transactional;
import org.shanoir.ng.subjectstudy.model.SubjectStudyTag;
import org.shanoir.ng.subjectstudy.model.SubjectStudyTagPk;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectStudyTagRepository extends CrudRepository<SubjectStudyTag, SubjectStudyTagPk> {

    @Modifying
    @Transactional
    @Query("DELETE FROM SubjectStudyTag sst WHERE sst.subjectStudy.subject.id = :subjectId")
    void deleteBySubjectId(@Param("subjectId") Long subjectId);

}
