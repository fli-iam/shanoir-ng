import { Reference } from '../../reference/shared/reference.model';
import { SubjectPathology } from '../../pathologies/subjectPathology/shared/subjectPathology.model';
import { SubjectTherapy } from '../../therapies/subjectTherapy/shared/subjectTherapy.model';

export class AnimalSubject {
  id: number;
  subjectId: number;
  specie: Reference;
  strain: Reference;
  biotype: Reference;
  provider : Reference;
  stabulation: Reference;
  
  pathologies: SubjectPathology[];
  therapies: SubjectTherapy[];
  
}
