import { Injectable } from '@angular/core';

import { Pathology } from './pathology.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';

@Injectable()
export class PathologyService extends EntityService<Pathology> {
         
    API_URL = PreclinicalUtils.PRECLINICAL_API_PATHOLOGIES_URL;

    getEntityInstance() { return new Pathology(); }   
           
    
    
}