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
import { Field } from '../shared/reflect/field.decorator';
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

    @Field() debugTs: number = Date.now();
    @Field() id: number;
    @Field() completeId: bigint;
    @Field() creationDate: Date;
    @Field() lastUpdate: Date;
    @Field() report: string;
    private _status: TaskStatus;
    private _message: string;
    private _progress: number;
    _eventType: string;
    @Field() eventLabel: string;
    @Field() objectId: number;
    @Field() route: string;
    @Field() hasReport: boolean;
    @Field() sessionId: string;
    _idAsString: string;
    @Field() hideFromMenu: boolean;
    private readonly FIELDS: string[] = ['id', 'creationDate', 'lastUpdate','_status','_message', '_progress', '_eventType', 'eventLabel', 'objectId', 'route', 'report', 'sessionId', '_idAsString'];

    set eventType(eventType: string) {
        this._eventType = eventType;
        this.eventLabel = camelToSpaces(this.eventType.replace('.event', ''));
    }

    @Field() get eventType(): string {
        return this._eventType;
    }

    set status(status: TaskStatus) {
        this._status = status;
        if (status == -1) this._progress = -1;
    }

    @Field() get status(): TaskStatus {
        return this._status;
    }

    set progress(progress: number) {
        if (this.status == -1) this._progress = -1;
        else this._progress = progress;
    }

    @Field() get progress(): number {
        return this._progress;
    }

    set message(message: string) {
        this._message = message;
        this.route = this.buildRoute();
    }

    @Field() get message(): string {
        return this._message;
    }

    @Field() get idAsString(): string {
        return this._idAsString;
    }

    set idAsString(idAsString: string) {
        this._idAsString = idAsString;
        this.route = this.buildRoute();
    }

    private buildRoute(): string {
        if (this.eventType === 'importDataset.event' && this.status != -1) {
            if (this.message.lastIndexOf('examination [') != -1) {
                const substring = this.message.match(/examination \[\d+\]/g)[0];
                return '/examination/details/' + substring.slice(substring.lastIndexOf("[") + 1, substring.lastIndexOf("]"));
            } else if (this.message.indexOf('dataset [') != -1) {
                const substring = this.message.match(/dataset \[\d+\]/g)[0];
                return '/dataset/details/' + substring.slice(substring.lastIndexOf("[") + 1, substring.lastIndexOf("]"));
            }
        } else if (this.eventType === 'executionMonitoring.event' && this.status != -1) {
            return '/dataset-processing/details/' + this.objectId
        } else if (this.eventType === 'solrIndexAll.event' && this.status != -1) {
            return '/solr-search';
        } else if (this.eventType === 'copyDataset.event' && this.status != -1 && this.message.lastIndexOf('study [') != -1) {
            return '/study/details/' + this.message.slice(this.message.lastIndexOf("[") + 1, this.message.lastIndexOf("]"));
        } else if (this.eventType === 'downloadStatistics.event' && this.status != -1 && this.status != 2) {
            return '/datasets/download/event/' + this.idAsString;
        }
        return null;
    }

    stringify(): string {
        return JSON.stringify(this, this.FIELDS);
    }

    clone(): Task {
        const clone: Task = new Task();
        this.FIELDS.forEach(fieldName => clone[fieldName] = this[fieldName]);
        return clone;
    }

    equals(task: Task) {
        for (const fieldName of this.FIELDS) {
            if (task[fieldName] != this[fieldName]) return false;
        }
        return true;
    }

    updateWith(task: Task): Task {
        if (task.status != undefined) this.status = task.status;
        if (task.progress != undefined) this.progress = task.progress;
        if (task.lastUpdate) this.lastUpdate = task.lastUpdate;
        if (!this.creationDate && task.creationDate) this.creationDate = task.creationDate;
        if (task.report) this.report = task.report;
        if (task.message) this.message = task.message;
        if (task.idAsString) this.idAsString = task.idAsString;
        if (task.hideFromMenu != undefined) this.hideFromMenu = task.hideFromMenu;
        if (task.sessionId) this.sessionId = task.sessionId;
        if (task.eventLabel) this.eventLabel = task.eventLabel;
        if (task.debugTs) this.debugTs = task.debugTs;
        if (task.objectId) this.objectId = task.objectId;
        if (task.route) this.route = task.route;
        if (task.hasReport != undefined) this.hasReport = task.hasReport;
        return this;
    }
}


