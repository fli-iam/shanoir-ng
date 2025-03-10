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
import { UntypedFormGroup } from '@angular/forms';
import { Option } from '../../shared/select/select.component';
import { StudyService } from '../../studies/shared/study.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { ActivatedRoute } from '@angular/router';
import { AccessRequestService } from './access-request.service';
import { IdName } from 'src/app/shared/models/id-name.model';

@Component({
    selector: 'access-request',
    templateUrl: 'access-request.component.html',
    styleUrls: ['access-request.component.css'],
    standalone: false
})

export class AccessRequestComponent extends EntityComponent<AccessRequest> {

    public studyOptions:  Option<number>[];
    studies: IdName[];
    fromStudy: boolean = false;

    public get accessRequest(): AccessRequest { return this.entity; }
    public set accessRequest(accreq: AccessRequest) {
        this.entity = accreq;
    }

    constructor(
            protected activatedRoute: ActivatedRoute,
            public userService: UserService,
            public studyService: StudyService,
            public accessRequestService: AccessRequestService) {
                super(activatedRoute, 'access-request');
            }

    public changeStudy(studyId: number) {
        this.accessRequest.studyName = this.studies[this.studies.findIndex(study => study.id == studyId)].name;
    }

    getService(): EntityService<AccessRequest> {
        return this.accessRequestService;
    }

    initCreate(): Promise<void> {
        this.accessRequest = new AccessRequest();
        if (this.activatedRoute.snapshot.params['id']) {
            this.accessRequest.studyId = this.activatedRoute.snapshot.params['id'];
            this.fromStudy = true;
            return Promise.resolve();
        }
        return this.studyService.getPublicStudiesConnected().then(result => {
            if (result && result.length > 0) {
                this.studies = result;
                this.studyOptions = result.map(element => new Option(element.id, element.name));
            } else {
                this.studies = [];
                this.studyOptions = [];
                this.confirmDialogService.error("No public study","No public studies available for the moment. If you want to join a private study, please ask the study manager to add you directly.")
                .then(value => this.goBack());
            }
        });
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
            'motivation': [this.accessRequest.motivation, []],
            'studyId': [this.accessRequest.studyId, []],
            'studyName': [this.accessRequest.studyName, []]
        });
    }

    initEdit(): Promise<void> {
        throw new Error('Should not be here');
    }

    initView(): Promise<void> {
        return this.studyService.getPublicStudiesConnected().then(studiesRes => {
            this.studies = studiesRes;
        });
    }

    acceptRequest() {
        this.accessRequestService.resolveRequest(this.accessRequest.id, true)
            .then(value => {
                this.userService.decreaseAccessRequests;
                this.router.navigate(['/study/details/' + this.accessRequest.studyId])
            }).then(() => {
                window.location.hash="members";
            }
        );
    }
    
    refuseRequest() {
        this.accessRequestService.resolveRequest(this.accessRequest.id, false).then(value => {
            this.userService.decreaseAccessRequests;
            this.goBack();
        });
    }

    public async hasDeleteRight(): Promise<boolean> {
        return false;
    }
    
    public async hasEditRight(): Promise<boolean> {
        return false;
    }
    
    save(): Promise<AccessRequest> {
        return super.save().then(ar => {
            return ar;
        });
    }

    protected chooseRouteAfterSave() {
        this.goBack();
    }
}
