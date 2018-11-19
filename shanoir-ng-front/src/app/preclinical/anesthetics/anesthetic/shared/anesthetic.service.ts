import { Injectable } from '@angular/core';
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';
import { Anesthetic } from './anesthetic.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class AnestheticService extends EntityService<Anesthetic>{
             
    API_URL = PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL;

    getEntityInstance() { return new Anesthetic(); }  
        
    
  
}