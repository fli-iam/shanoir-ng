/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
