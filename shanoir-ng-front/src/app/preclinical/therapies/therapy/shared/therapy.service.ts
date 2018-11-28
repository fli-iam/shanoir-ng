import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';


import { Therapy } from './therapy.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class TherapyService  extends EntityService<Therapy>{
         
    API_URL = PreclinicalUtils.PRECLINICAL_API_THERAPIES_URL;

    getEntityInstance() { return new Therapy(); }
     
    
}