import {Injectable, OnDestroy} from "@angular/core";
import {EntityService} from "../../shared/components/entity/entity.abstract.service";
import * as AppUtils from "../../utils/app.utils";
import { HttpClient } from "@angular/common/http";
import {ShanoirEvent} from "./shanoir-event.model";
import {Page, Pageable} from "../../shared/components/table/pageable.model";
import {DatasetAcquisition} from "../../dataset-acquisitions/shared/dataset-acquisition.model";
import {DatasetAcquisitionDTO} from "../../dataset-acquisitions/shared/dataset-acquisition.dto";

@Injectable()
export class ShanoirEventService extends EntityService<ShanoirEvent> implements OnDestroy {

    API_URL = AppUtils.BACKEND_API_USER_EVENTS;

    constructor(protected http: HttpClient) {
        super(http);
    }

    getPage(pageable : Pageable, studyId: number, searchStr : string, searchField : string): Promise<Page<ShanoirEvent>> {
        let params = { 'params': pageable.toParams() };
        params['params']['searchStr'] = searchStr;
        params['params']['searchField'] = searchField;
        return this.http.get<Page<ShanoirEvent>>(this.API_URL + '/' + studyId, params)
            .toPromise()
            .then(this.mapPage);
    }

    getEntityInstance(entity?: ShanoirEvent): ShanoirEvent {
        return new ShanoirEvent();
    }
}
