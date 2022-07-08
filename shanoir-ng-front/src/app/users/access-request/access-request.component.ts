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

import { Location } from '@angular/common';
import { Component } from '@angular/core';

import * as AppUtils from '../../utils/app.utils';
import { User } from '../shared/user.model';
import { AccountRequestInfo } from '../account-request-info/account-request-info.model';
import { UserService } from '../shared/user.service'
import { AccessRequest } from './access-request.model'
import { FormGroup, Validators, FormBuilder, AbstractControl, ValidationErrors } from '@angular/forms';
import { Option } from '../../shared/select/select.component';
import { StudyService } from '../../studies/shared/study.service';

@Component({
    selector: 'accountRequest',
    templateUrl: 'access-request.component.html',
    styleUrls: ['access-request.component.css']
})

export class AccessRequestComponent {

    public form: FormGroup;
    public accessRequest: AccessRequest;
    public studyOptions:  Option<number>[];

    constructor(
            private fb: FormBuilder, 
            public userService: UserService,
            public studyService: StudyService,
            private location: Location) {
                this.accessRequest = new AccessRequest();
            }

    buildForm(): void {
        this.form = this.fb.group({
            'invitationKey': [this.accessRequest.invitationKey, []],
            'motivation': [this.accessRequest.motivation, []],
            'studyId': [this.accessRequest.studyId, []]
        });
    }

    ngOnInit() {
        this.studyService.getPublicStudies().then(result => {
            if (result) {
                this.studyOptions = result.map(element => new Option(element.id, element.name));
            } else {
                this.studyOptions = [];
            }
        });
    }
    
}
