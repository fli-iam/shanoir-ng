import { Injectable } from '@angular/core';

import { Examination } from '../../../examinations/shared/examination.model';
import * as AppUtils from '../../../utils/app.utils';
import { EntityService } from '../../../shared/components/entity/entity.abstract.service';
import { Page, Pageable } from '../../../shared/components/table/pageable.model';


@Injectable()
export class AnimalExaminationService extends EntityService<Examination>{
    API_URL = AppUtils.BACKEND_API_EXAMINATION_URL;

    getEntityInstance() { return new Examination(); }

    getPage(pageable: Pageable): Promise<Page<Examination>> {
        return this.http.get<Page<Examination>>(
            AppUtils.BACKEND_API_EXAMINATION_PRECLINICAL_URL+'/1', 
            { 'params': pageable.toParams() }
        ).toPromise();
    }   
}