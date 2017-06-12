package org.shanoir.ng.subject;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.dto.SubjectStudyDTO;

@Mapper(componentModel= "spring",uses={})
@DecoratedWith(SubjectDecorator.class)
public interface SubjectMapper {

  //  SubjectMapper INSTANCE = Mappers.getMapper(SubjectMapper.class);

   // @Mapping(source = "subject", target = "subjectDTO")
   // @Mappings({
   @Mappings({
   @Mapping(target = "subjectStudyList",ignore=true),
   @Mapping(target = "imagedObjectCategory",ignore=true),
   @Mapping(target="identifier", source="subjectIdentifier")})
   Subject subjectDTOToSubject(SubjectDTO subjectDTO);
    //
    // //@InheritInverseConfiguration
    // @Mappings({
    // @Mapping(source = "refSex.labelName", target = "sex"),
    // @Mapping(target = "subjectStudyList",ignore=true)})
    // SubjectDTO subjectToSubjectDTO(Subject subject);

    //
    // @Mappings({
    // @Mapping(source = "studyId", target = "study.id")})
    @Mappings({
    @Mapping(target = "subjectType",ignore=true)})
    SubjectStudy subjectStudyDTOToSubjectStudy(SubjectStudyDTO subjectStudyDTO);

    // @Mappings({
    // @Mapping(source = "study.id", target = "studyId"),
    // @Mapping(source="refSubjectType.labelName", target= "subjectType")})
    // SubjectStudyDTO SubjectStudyTosubjectStudyDTO(SubjectStudy subjectStudy);
    //

//    List<RelSubjectStudy> subjectStudyDTOListToRelSubjectStudyList(List<SubjectStudyDTO> subjectStudyDTO);
//
//    List<SubjectStudyDTO> relSubjectStudyListToSubjectStudyDTOList(List<RelSubjectStudy> subjectStudyList);
//
//

}
