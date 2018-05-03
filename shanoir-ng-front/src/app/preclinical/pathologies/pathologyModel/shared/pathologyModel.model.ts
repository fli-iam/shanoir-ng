import { Pathology } from '../../pathology/shared/pathology.model';

export class PathologyModel {
  id: number;
  name: string;
  comment: string;
  filename: string;
  pathology: Pathology;
}

