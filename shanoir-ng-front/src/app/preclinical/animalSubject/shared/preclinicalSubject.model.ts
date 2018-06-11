import { AnimalSubject } from './animalSubject.model';
import { Subject }    from '../../../subjects/shared/subject.model';
import { SubjectPathology } from '../../pathologies/subjectPathology/shared/subjectPathology.model';
import { SubjectTherapy } from '../../therapies/subjectTherapy/shared/subjectTherapy.model';

export class PreclinicalSubject {
  id: number;
  subject: Subject;
  animalSubject: AnimalSubject;
  pathologies: SubjectPathology[];
  therapies: SubjectTherapy[];
  
}
