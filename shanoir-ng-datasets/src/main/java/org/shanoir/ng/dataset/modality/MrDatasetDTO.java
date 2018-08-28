package org.shanoir.ng.dataset.modality;

import java.util.List;
import org.shanoir.ng.dataset.DatasetDTO;
import org.shanoir.ng.shared.model.EchoTime;
import org.shanoir.ng.shared.model.FlipAngle;
import org.shanoir.ng.shared.model.InversionTime;
import org.shanoir.ng.shared.model.RepetitionTime;


public class MrDatasetDTO extends DatasetDTO {
    
    private List<EchoTime> echoTime;
    
    private List<FlipAngle> flipAngle;
    
    private List<InversionTime> inversionTime;
    
    private List<RepetitionTime> repetitionTime;


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
}
