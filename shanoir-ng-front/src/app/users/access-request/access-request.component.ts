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

import { Component } from '@angular/core';
import { UserService } from '../shared/user.service'
import { AccessRequest } from './access-request.model'
import { FormGroup } from '@angular/forms';
import { Option } from '../../shared/select/select.component';
import { StudyService } from '../../studies/shared/study.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { ActivatedRoute } from '@angular/router';
import { ModesAware } from "../../preclinical/shared/mode/mode.decorator";


@Component({
    selector: 'accountRequest',
    templateUrl: 'access-request.component.html',
    styleUrls: ['access-request.component.css']
})
@ModesAware
export class AccessRequestComponent extends EntityComponent<AccessRequest> {

    constructor(
            protected activatedRoute: ActivatedRoute,
            public userService: UserService,
            public studyService: StudyService) {
                super(activatedRoute, 'access-request');
            }

    public accessRequest: AccessRequest;
    public studyOptions:  Option<number>[];

    getService(): EntityService<AccessRequest> {
        return null;
    }

    initCreate(): Promise<void> {
        this.accessRequest = new AccessRequest();

        return this.studyService.getPublicStudies().then(result => {
            if (result) {
                this.studyOptions = result.map(element => new Option(element.id, element.name));
            } else {
                this.studyOptions = [];
            }
        });
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'motivation': [this.accessRequest.motivation, []],
            'studyId': [this.accessRequest.studyId, []]
        });
    }

    initEdit(): Promise<void> {
        throw new Error('Should not be here');
    }
    initView(): Promise<void> {
        throw new Error('Should not be here');
    }
}
