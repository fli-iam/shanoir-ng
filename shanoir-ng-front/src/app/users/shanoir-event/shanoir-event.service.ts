import {Injectable, OnDestroy} from "@angular/core";
import {EntityService} from "../../shared/components/entity/entity.abstract.service";
import * as AppUtils from "../../utils/app.utils";
import {HttpClient} from "@angular/common/http";
import {ShanoirEvent} from "./shanoir-event.model";

@Injectable()
export class ShanoirEventService extends EntityService<ShanoirEvent> implements OnDestroy {

    API_URL = AppUtils.BACKEND_API_USER_EVENTS;

    constructor(protected http: HttpClient) {
        super(http);
    }

    requestHistory(studyId: number): Promise<ShanoirEvent[]> {
        return this.http.get<ShanoirEvent[]>(this.API_URL + '/' + studyId)
            .toPromise();
    }

    getEntityInstance(entity?: ShanoirEvent): ShanoirEvent {
        return new ShanoirEvent();
    }
}
