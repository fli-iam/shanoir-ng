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
import { Report } from '../shared/mass-download/mass-download.service';
import { camelToSpaces } from '../utils/app.utils';

export class Task extends Entity {

    id: number;
    creationDate: Date;
    lastUpdate: Date;
    private _status: -1 | 1 | 2;
    private _message: string;
    private _progress: number;
    private _eventType: string;
    eventLabel: string;
    objectId: number;
    route: string;
    private readonly FIELDS: string[] = ['id', 'creationDate', 'lastUpdate','_status','_message', '_progress', '_eventType', 'eventLabel', 'objectId', 'route'];

    set eventType(eventType: string) {
        this._eventType = eventType;
        this.eventLabel = camelToSpaces(this.eventType.replace('.event', ''));
    }

    get eventType(): string {
        return this._eventType;
    }

    set status(status: -1 | 1 | 2) {
        this._status = status;
        if (status == -1) this._progress = -1;
    }

    get status(): -1 | 1 | 2 {
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
            if (this.message.lastIndexOf('in examination ') != -1) {
                return '/examination/details/' + this.message.slice(this.message.lastIndexOf('in examination ') + ('in examination '.length));
            } else if (this.message.lastIndexOf('for examination ') != -1) {
                return '/examination/details/' + this.message.slice(this.message.lastIndexOf('for examination ') + ('for examination '.length));
            } else if (this.message.indexOf('in dataset') != -1) {
                return '/dataset/details/' + this.message.slice(this.message.lastIndexOf('in dataset ') + ('in dataset '.length));
            } else if (this.message.indexOf('VIP Execution') != -1) {
               return '/dataset-processing/details/' + this.objectId
            }
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


export class DownloadTask extends Task {
    
    report: Report;
}


