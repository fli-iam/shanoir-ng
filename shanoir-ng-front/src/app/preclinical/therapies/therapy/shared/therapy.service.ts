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
import { Observable } from 'rxjs/Observable';
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';


import { Therapy } from './therapy.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class TherapyService  extends EntityService<Therapy>{
         
    API_URL = PreclinicalUtils.PRECLINICAL_API_THERAPIES_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance() { return new Therapy(); }
}