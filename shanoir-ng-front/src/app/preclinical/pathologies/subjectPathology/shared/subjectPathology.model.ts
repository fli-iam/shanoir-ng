import { Reference } from '../../../reference/shared/reference.model';
import { Pathology } from '../../pathology/shared/pathology.model';
import { PathologyModel } from '../../pathologyModel/shared/pathologyModel.model';
import { Entity } from "../../../../shared/components/entity/entity.abstract";
import { ServiceLocator } from "../../../../utils/locator.service";
import { SubjectPathologyService } from './subjectPathology.service';

export class SubjectPathology  extends Entity{
  id: number;
  pathology: Pathology;
  pathologyModel: PathologyModel;
  location: Reference;
  startDate: Date;
  endDate : Date;


  service: SubjectPathologyService = ServiceLocator.injector.get(SubjectPathologyService);
}

