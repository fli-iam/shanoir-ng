/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

import { Entity } from '../shared/components/entity/entity.abstract';
import { camelToSpaces } from '../utils/app.utils';

export enum TaskStatus {
    ERROR = -1,
    DONE = 1,
    IN_PROGRESS = 2,
    DONE_BUT_WARNING = 3,
    QUEUED = 4,
    IN_PROGRESS_BUT_WARNING = 5
}

export class TaskState {

    errors: any;

    constructor(public status?: TaskStatus, public progress?: number) {}

    isActive(): boolean {
        return [TaskStatus.IN_PROGRESS, TaskStatus.QUEUED, TaskStatus.IN_PROGRESS_BUT_WARNING].includes(this.status);
    }
}

export class Task extends Entity {

    debugTs: number = Date.now();
    id: number;
    completeId: BigInt;
    creationDate: Date;
    lastUpdate: Date;
    report: string;
    private _status: TaskStatus;
    private _message: string;
    private _progress: number;
    _eventType: string;
    eventLabel: string;
    objectId: number;
    route: string;
    hasReport: boolean;
    private readonly FIELDS: string[] = ['id', 'creationDate', 'lastUpdate','_status','_message', '_progress', '_eventType', 'eventLabel', 'objectId', 'route', 'report'];

    set eventType(eventType: string) {
        this._eventType = eventType;
        this.eventLabel = camelToSpaces(this.eventType.replace('.event', ''));
    }

    get eventType(): string {
        return this._eventType;
    }

    set status(status: TaskStatus) {
        this._status = status;
        if (status == -1) this._progress = -1;
    }

    get status(): TaskStatus {
        return this._status;
    }

    set progress(progress: number) {
        if (this.status == -1) this._progress = -1;
        else this._progress = progress;
    }

    get progress(): number {
        return this._progress;
    }

    set message(message: string) {
        this._message = message;
        this.route = this.buildRoute();
    }

    get message(): string {
        return this._message;
    }

    private buildRoute(): string {
        if (this.eventType === 'importDataset.event' && this.status != -1) {
            if (this.message.lastIndexOf('examination [') != -1) {
                let substring = this.message.match(/examination \[\d+\]/g)[0];
                return '/examination/details/' + substring.slice(substring.lastIndexOf("[") + 1, substring.lastIndexOf("]"));
            } else if (this.message.indexOf('dataset [') != -1) {
                let substring = this.message.match(/dataset \[\d+\]/g)[0];
                return '/dataset/details/' + substring.slice(substring.lastIndexOf("[") + 1, substring.lastIndexOf("]"));
            }
        } else if (this.eventType === 'executionMonitoring.event' && this.status != -1) {
            return '/dataset-processing/details/' + this.objectId
        } else if (this.eventType === 'copyDataset.event' && this.status != -1 && this.message.lastIndexOf('study [') != -1) {
            return '/study/details/' + this.message.slice(this.message.lastIndexOf("[") + 1, this.message.lastIndexOf("]"));
        }
        return null;
    }

    stringify(): string {
        return JSON.stringify(this, this.FIELDS);
    }

    clone(): Task {
        let clone: Task = new Task();
        this.FIELDS.forEach(fieldName => clone[fieldName] = this[fieldName]);
        return clone;
    }

    equals(task: Task) {
        for (let fieldName of this.FIELDS) {
            if (task[fieldName] != this[fieldName]) return false;
        }
        return true;
    }
}


