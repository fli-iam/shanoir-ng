package org.shanoir.ng.shared.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.model.SubjectDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubjectMapper {

	List<Subject> EchoTimeDTOListToEchoTimeList(
			List<SubjectDTO> echoTimeDTOList);

	Subject EchoTimeDTOToEchoTime(
			SubjectDTO echoTimes);
}
