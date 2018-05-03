import { Reference } from '../../../reference/shared/reference.model';
import { Pathology } from '../../pathology/shared/pathology.model';
import { PathologyModel } from '../../pathologyModel/shared/pathologyModel.model';

export class SubjectPathology {
  id: number;
  pathology: Pathology;
  pathologyModel: PathologyModel;
  location: Reference;
  startDate: Date;
  endDate : Date;
}

