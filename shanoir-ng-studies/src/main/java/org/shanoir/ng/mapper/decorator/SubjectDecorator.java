package org.shanoir.ng.mapper.decorator;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.subject.Subject;
import org.shanoir.ng.subject.ImagedObjectCategory;
import org.shanoir.ng.subject.SubjectStudy;
import org.shanoir.ng.study.StudyRepository;
import org.shanoir.ng.subject.dto.SubjectStudyDTO;
import org.shanoir.ng.mapper.SubjectMapper;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.dto.SubjectStudyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.shanoir.ng.subject.Sex;

public abstract class SubjectDecorator implements SubjectMapper {

    @Autowired
    @Qualifier("delegate")
    private SubjectMapper delegate;

    @Autowired
    private StudyRepository studyRepository;

    @Override
    public Subject subjectDTOToSubject(SubjectDTO subjectDTO){
      Subject subject = delegate.subjectDTOToSubject(subjectDTO);

      // Subject Study List manual mapping
      if (!subjectDTO.getSubjectStudyList().isEmpty()){
          for (SubjectStudyDTO s : subjectDTO.getSubjectStudyList())  {
            SubjectStudy subjectStudy = delegate.subjectStudyDTOToSubjectStudy(s);
            subject.addSubjectStudy(subjectStudy);
            subjectStudy.setSubject(subject);
        }
      }

      // Subject Image Object Category manual mapping
      if (subjectDTO.getImagedObjectCategory() != null){
        for (ImagedObjectCategory i : ImagedObjectCategory.values()){
          if (i.getValue().equals(subjectDTO.getImagedObjectCategory())){
            subject.setImagedObjectCategory(i);
          }
        }
      }
      return subject;
    }


    @Override
    public SubjectStudy subjectStudyDTOToSubjectStudy(SubjectStudyDTO subjectStudyDTO){
      SubjectStudy subjectStudy = delegate.subjectStudyDTOToSubjectStudy(subjectStudyDTO);
      if (subjectStudyDTO.getStudyId() != null){
        subjectStudy.setStudy(studyRepository.findById(subjectStudyDTO.getStudyId()));
      }
      return subjectStudy;
    }



    //
    // @Override
    // public Subject subjectDTOToSubject(SubjectDTO subjectDTO){
    //     Subject subject = delegate.subjectDTOToSubject(subjectDTO);
    //
    // }
    //
    // @Override
    // public SubjectDTO subjectToSubjectDTO(Subject subject) {
    //     SubjectDTO dto = delegate.subjectToSubjectDTO(subject);
    //     List<SubjectStudyDTO> studyList = new ArrayList<SubjectStudyDTO>();
    //     for (RelSubjectStudy r : subject.getRelSubjectStudyList()){
    //         SubjectStudyDTO s = this.INSTANCE.RelSubjectStudyTosubjectStudyDTO(r);
    //         studyList.add(s);
    //     }
    //     if (studyList != null){
    //         dto.setSubjectStudyList(studyList);
    //     }
    //     return dto;
    // }

}
