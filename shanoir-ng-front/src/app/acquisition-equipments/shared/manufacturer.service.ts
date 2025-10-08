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

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import * as AppUtils from '../../utils/app.utils';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import {ServiceLocator} from "../../utils/locator.service";
import {ConsoleService} from "../../shared/console/console.service";
import {Entity} from "../../shared/components/entity/entity.abstract";

import { Manufacturer } from './manufacturer.model';

@Injectable()
export class ManufacturerService extends EntityService<Manufacturer> {

    API_URL = AppUtils.BACKEND_API_MANUF_URL;
    protected consoleService = ServiceLocator.injector.get(ConsoleService);

    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance() { return new Manufacturer(); }

    deleteWithConfirmDialog(name: string, entity: Entity): Promise<boolean> {
        const warn = 'The ' + name + ' with id ' + entity.id + ' is linked to other entities, it was not deleted.';

        this.consoleService.log('warn', warn);
        return Promise.resolve(false);
    }
}
