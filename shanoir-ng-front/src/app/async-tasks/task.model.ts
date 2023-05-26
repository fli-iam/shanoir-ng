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

export class Task extends Entity {

    id: number;
    creationDate: Date;
    lastUpdate: Date;
    status: number;
    message: string;
    progress: number;
    _eventType: string;
    eventLabel: string;
    objectId: number;
    timestamp: number;

    set eventType(eventType: string) {
        this._eventType = eventType;
        this.eventLabel = camelToSpaces(this.eventType.replace('.event', ''));
    }

    get eventType(): string {
        return this._eventType;
    }
}
