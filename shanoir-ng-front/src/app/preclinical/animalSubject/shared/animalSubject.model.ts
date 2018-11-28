import { Reference } from '../../reference/shared/reference.model';
import { Entity } from "../../../shared/components/entity/entity.abstract";
import { ServiceLocator } from "../../../utils/locator.service";
import { AnimalSubjectService } from './animalSubject.service';

export class AnimalSubject extends Entity {
  id: number;
  subjectId: number;
  specie: Reference;
  strain: Reference;
  biotype: Reference;
  provider : Reference;
  stabulation: Reference;
  

  service: AnimalSubjectService = ServiceLocator.injector.get(AnimalSubjectService);
  
}
