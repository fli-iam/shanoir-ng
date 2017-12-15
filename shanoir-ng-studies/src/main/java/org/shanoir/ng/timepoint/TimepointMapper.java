package org.shanoir.ng.timepoint;

import java.util.List;

import org.mapstruct.Mapper;

/**
 * Mapper for time point.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring")
public interface TimepointMapper {

	/**
	 * Map list of @Timepoint to list of @TimepointDTO.
	 * 
	 * @param timepoints
	 *            list of time points.
	 * @return list of DTO.
	 */
	List<TimepointDTO> timepointsToTimepointDTOs(List<Timepoint> timepoints);

	/**
	 * Map a @Timepoint to a @TimepointDTO.
	 * 
	 * @param timepoint
	 *            time point.
	 * @return DTO.
	 */
	TimepointDTO timepointToTimepointDTO(Timepoint timepoint);

}
