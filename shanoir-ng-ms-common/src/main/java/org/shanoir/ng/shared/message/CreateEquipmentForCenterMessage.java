package org.shanoir.ng.shared.message;

import org.shanoir.ng.shared.dicom.EquipmentDicom;

public class CreateEquipmentForCenterMessage {

    private Long centerId;

    private EquipmentDicom equipmentDicom;

    public CreateEquipmentForCenterMessage() {}

    public CreateEquipmentForCenterMessage(Long centerId, EquipmentDicom equipmentDicom) {
        this.centerId = centerId;
        this.equipmentDicom = equipmentDicom;
    }

    public Long getCenterId() {
        return centerId;
    }

    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }

    public EquipmentDicom getEquipmentDicom() {
        return equipmentDicom;
    }

    public void setEquipmentDicom(EquipmentDicom equipmentDicom) {
        this.equipmentDicom = equipmentDicom;
    }

}
