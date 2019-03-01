import { Injectable } from '@angular/core';

import { EntityService } from '../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../utils/app.utils';
import { Task } from './task.model';

@Injectable()
export class TaskService extends EntityService<Task> {

    API_URL = AppUtils.BACKEND_API_TASKS_URL;

    getEntityInstance() { return new Task(); }

}