import { AnimalSubject } from './animalSubject.model';
import { Subject }    from '../../../subjects/shared/subject.model';
import { SubjectPathology } from '../../pathologies/subjectPathology/shared/subjectPathology.model';
import { SubjectTherapy } from '../../therapies/subjectTherapy/shared/subjectTherapy.model';
import { Entity } from "../../../shared/components/entity/entity.abstract";
import { ServiceLocator } from "../../../utils/locator.service";
import { AnimalSubjectService } from './animalSubject.service';

export class PreclinicalSubject extends Entity {
  id: number;
  subject: Subject;
  animalSubject: AnimalSubject;
  pathologies: SubjectPathology[];
  therapies: SubjectTherapy[];

  service: AnimalSubjectService = ServiceLocator.injector.get(AnimalSubjectService);
  
}
