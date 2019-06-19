import { Injectable } from '@angular/core';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { IdName } from '../../shared/models/id-name.model';
import * as AppUtils from '../../utils/app.utils';
import { Center } from './center.model';
import { Study } from '../../studies/shared/study.model';
import { StudyCenter } from 'src/app/studies/shared/study-center.model';

@Injectable()
export class CenterService extends EntityService<Center> {

    API_URL = AppUtils.BACKEND_API_CENTER_URL;

    getEntityInstance() { return new Center(); }

    getCentersNames(): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL)
            .toPromise();
    }

    getCentersNamesByStudyId(studyId: number): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL + "/" + studyId)
            .toPromise();
    }

    getCentersNamesForExamination(): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL)
            .toPromise();
    }
}