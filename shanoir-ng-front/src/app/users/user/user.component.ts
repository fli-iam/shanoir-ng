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
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Role } from '../../roles/role.model';
import { RoleService } from '../../roles/role.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { DatepickerComponent } from '../../shared/date-picker/date-picker.component';
import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import * as AppUtils from '../../utils/app.utils';
import { StudyService } from 'src/app/studies/shared/study.service';


@Component({
    selector: 'user-detail',
    templateUrl: 'user.component.html',
    styleUrls: ['user.component.css']
})

export class UserComponent extends EntityComponent<User> {

    public roles: Role[];
    public denyLoading: boolean = false;
    public acceptLoading: boolean = false;
    public studies = [];

    constructor(
            private route: ActivatedRoute,
            private userService: UserService,
            private roleService: RoleService,
            private studyService: StudyService) {
            
        super(route, 'user');
    }

    public get user(): User { return this.entity; }
    public set user(user: User) { this.entity = user; }

    getService(): EntityService<User> {
        return this.userService;
    }

    initView(): Promise<void> {
        return this.loadUserAndRoles();
    }

    initEdit(): Promise<void> {
        return this.loadUserAndRoles();
    }

    initCreate(): Promise<void> {
        this.user = new User();
        this.getRoles();
        return Promise.resolve();
    }

    private loadUserAndRoles(): Promise<void> {
        let userPromise: Promise<void> = this.userService.get(this.id).then((user: User) => {
            this.user = user;
            if (user.extensionRequestDemand && user.extensionRequestInfo) {
                this.user.expirationDate = user.extensionRequestInfo.extensionDate;
            }
        });
        this.studyService.findStudiesByUserId().then(studies => {
            this.studies = studies.filter(study =>  {
                for( var suser of study.studyUserList) {
                    // Admin case, we check that the user is part of the study
                    return suser.userId === this.entity.id;
                }
            });
        });
        Promise.all([userPromise, this.getRoles()]).then(() => {
            this.user.role = this.getRoleById(this.user.role.id);
        });
        return userPromise;
    }

    getRoles(): Promise<void> {
        return this.roleService.getRoles().then(roles => {
            this.roles = roles;
        });
    }

    accept(): void {
        this.acceptLoading = true;
        this.userService.confirmAccountRequest(this.id, this.user)
            .then((user) => {
                this.msgBoxService.log('info', 'User saved and confirmed !');
                this.goBack();
                this.acceptLoading = false;
            }).catch(reason => {
                this.acceptLoading = false;
                throw reason;
            });
    }

    deny(): void {
        this.denyLoading = true;
        this.userService.denyAccountRequest(this.id)
            .then((user) => {
                this.msgBoxService.log('info', 'The request has been denied !');
                this.goBack();
                this.denyLoading = false;
            }).catch(reason => {
                this.denyLoading = false;
                throw reason;
            });
    }

    buildForm(): FormGroup {
        const emailRegex = '^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$';
        let userForm = this.formBuilder.group({
            'firstName': [this.user.firstName, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
            'lastName': [this.user.lastName, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
            'email': [this.user.email, [Validators.required, Validators.pattern(emailRegex), this.registerOnSubmitValidator('unique', 'email')]],
            'expirationDate': [this.user.expirationDate],
            'extensionMotivation': [this.user.extensionRequestInfo ? this.user.extensionRequestInfo.extensionMotivation : ''],
            'role': [this.user.role, [Validators.required]],
            'canAccessToDicomAssociation': new FormControl('false'),
            'accountRequestInfo': [this.user.accountRequestInfo]
        });
        if (this.user.extensionRequestDemand) {
            userForm.get('expirationDate').setValidators([DatepickerComponent.validator, Validators.required, DatepickerComponent.inFutureValidator]);
        } else {
            userForm.get('expirationDate').setValidators([DatepickerComponent.validator, Validators.required]);
        }
        return userForm;
    }

    getRoleById(id: number): Role {
        for (let role of this.roles) {
            if (id == role.id) {
                return role;
            }
        }
        return null;
    }

    public async hasDeleteRight(): Promise<boolean> {
        return false;
    }

    isUserAdmin(): boolean {
        return this.keycloakService.isUserAdmin();
    }

}
