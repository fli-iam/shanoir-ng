import { Pathology } from '../../pathology/shared/pathology.model';
import { Entity } from "../../../../shared/components/entity/entity.abstract";
import { PathologyModelService } from './pathologyModel.service';
import { ServiceLocator } from "../../../../utils/locator.service";

export class PathologyModel extends Entity {
  id: number;
  name: string;
  comment: string;
  filename: string;
  pathology: Pathology;

  service: PathologyModelService = ServiceLocator.injector.get(PathologyModelService);
}

