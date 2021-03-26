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

package org.shanoir.ng.dataset.modality;

import java.util.List;

import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.shared.model.EchoTime;
import org.shanoir.ng.shared.model.FlipAngle;
import org.shanoir.ng.shared.model.InversionTime;
import org.shanoir.ng.shared.model.RepetitionTime;


public class MrDatasetDTO extends DatasetDTO {
    
    private List<EchoTime> echoTime;
    
    private List<FlipAngle> flipAngle;
    
    private List<InversionTime> inversionTime;
    
    private List<RepetitionTime> repetitionTime;
    
	private MrDatasetMetadata originMrMetadata;
    
	private MrDatasetMetadata updatedMrMetadata;


    public List<EchoTime> getEchoTime() {
        return this.echoTime;
    }

    public void setEchoTime(List<EchoTime> echoTime) {
        this.echoTime = echoTime;
    }

    public List<FlipAngle> getFlipAngle() {
        return this.flipAngle;
    }

    public void setFlipAngle(List<FlipAngle> flipAngle) {
        this.flipAngle = flipAngle;
    }

    public List<InversionTime> getInversionTime() {
        return this.inversionTime;
    }

    public void setInversionTime(List<InversionTime> inversionTime) {
        this.inversionTime = inversionTime;
    }

    public List<RepetitionTime> getRepetitionTime() {
        return this.repetitionTime;
    }

    public void setRepetitionTime(List<RepetitionTime> repetitionTime) {
        this.repetitionTime = repetitionTime;
    }

	public MrDatasetMetadata getOriginMrMetadata() {
		return originMrMetadata;
	}

	public void setOriginMrMetadata(MrDatasetMetadata originMrMetadata) {
		this.originMrMetadata = originMrMetadata;
	}

	public MrDatasetMetadata getUpdatedMrMetadata() {
		return updatedMrMetadata;
	}

	public void setUpdatedMrMetadata(MrDatasetMetadata updatedMrMetadata) {
		this.updatedMrMetadata = updatedMrMetadata;
	}
}
