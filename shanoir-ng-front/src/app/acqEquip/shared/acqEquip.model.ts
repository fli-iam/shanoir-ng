import { Center } from '../../centers/shared/center.model';
import { ManufacturerModel } from './manufModel.model';

export class AcquisitionEquipment {
    id: number;
    serialNumber: string;
    center: Center;
    manufacturerModel: ManufacturerModel;
}