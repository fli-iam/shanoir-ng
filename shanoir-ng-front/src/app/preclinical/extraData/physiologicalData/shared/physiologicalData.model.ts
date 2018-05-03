import { ExtraData } from '../../extraData/shared/extradata.model';

export class PhysiologicalData extends ExtraData {
  has_heart_rate:boolean = false;
  has_respiratory_rate:boolean = false;
  has_sao2:boolean = false;
  has_temperature:boolean = false;
}

