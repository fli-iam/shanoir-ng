import { AcquisitionEquipment } from "../../acquisition-equipments/shared/acquisition-equipment.model";

export class Center {
    acquisitionEquipments: AcquisitionEquipment[];
    city: string;
    country: string;
    id: number;
    name: string;
    phoneNumber: string;
    postalCode: string;
    street: string;
    website: string;
}