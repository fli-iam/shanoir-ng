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
import {
    AbstractControl,
    UntypedFormControl,
    UntypedFormGroup,
    ValidationErrors,
    ValidatorFn,
    Validators
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { StudyUser } from 'src/app/studies/shared/study-user.model';
import { StudyService } from 'src/app/studies/shared/study.service';
import { Selection } from 'src/app/studies/study/tree.service';
import { Role } from '../../roles/role.model';
import { RoleService } from '../../roles/role.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { DatepickerComponent } from '../../shared/date-picker/date-picker.component';
import { Study } from "../../studies/shared/study.model";
import { KEYCLOAK_BASE_URL } from "../../utils/app.utils";
import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';
import {dateDisplay} from "../../shared/./localLanguage/localDate.abstract";

@Component({
    selector: 'user-detail',
    templateUrl: 'user.component.html',
    styleUrls: ['user.component.css'],
    standalone: false
})

export class UserComponent extends EntityComponent<User> {

    public roles: Role[];
    public denyLoading: boolean = false;
    public acceptLoading: boolean = false;
    public studies = [];
    public studyToDelete = [];
    protected showTreeByDefault: boolean = false;
    public dateDisplay = dateDisplay;

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

    protected fetchEntity: () => Promise<User> = () => {
        return this.idPromise.then(id => {
            return this.getService().get(id).then(user => {
                return this.loadRolesAndStudies(user).then(() => user);
            });
        });
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromUser(this.user);
    }

    initView(): Promise<void> {
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.user = new User();
        this.getRoles();
        return Promise.resolve();
    }

    private loadRolesAndStudies(user: User): Promise<void> {
        if (user.extensionRequestDemand && user.extensionRequestInfo) {
            user.expirationDate = user.extensionRequestInfo.extensionDate;
        }
        const studyUsersPromise: Promise<void> = this.studyService.findStudiesByUserId().then(studies => {
            const studyUserList: StudyUser[] = [];
            this.studies = [];
            studies.forEach(s => {
                s.studyUserList.forEach(su => {
                    if (su.userId == user.id) {
                        studyUserList.push(su);
                        this.studies.push(s);
                    }
                })
            });
            user.studyUserList = studyUserList;
        });
        const rolesPromise = this.getRoles().then(() => {
            user.role = this.getRoleById(user.role.id);
        });
        return Promise.all([studyUsersPromise, rolesPromise]).then();
    }

    getRoles(): Promise<void> {
        return this.roleService.getRoles().then(roles => {
            this.roles = roles;
        });
    }

    accept(): void {
        this.acceptLoading = true;
        this.userService.confirmAccountRequest(this.id, this.user)
            .then(() => {
                this.consoleService.log('info', 'User "' + this.user.username + '" saved and confirmed !');
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
            .then(() => {
                this.consoleService.log('info', 'The request for user "' + this.user.username + '" has been denied !');
                this.goBack();
                this.denyLoading = false;
            }).catch(reason => {
                this.denyLoading = false;
                throw reason;
            });
    }

    buildForm(): UntypedFormGroup {
        const emailRegex = /^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/;
        let userForm = this.formBuilder.group({
            'firstName': [this.user.firstName, [Validators.required, Validators.minLength(2), Validators.maxLength(50), this.usualCharsValidator()]],
            'lastName': [this.user.lastName, [Validators.required, Validators.minLength(2), Validators.maxLength(50), this.usualCharsValidator()]],
            'email': [this.user.email, [Validators.required, Validators.pattern(emailRegex), this.registerOnSubmitValidator('unique', 'email')]],
            'expirationDate': [this.user.expirationDate],
            'extensionMotivation': [this.user.extensionRequestInfo ? this.user.extensionRequestInfo.extensionMotivation : ''],
            'role': [this.user.role, [Validators.required]],
            'canAccessToDicomAssociation': new UntypedFormControl('false'),
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
        for (const role of this.roles) {
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

    removeStudyFromUser(study: Study, index: number) {
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
        if (index !== null) {
            this.studyToDelete.push(this.studies.find(x => x.id === study.id));
            this.studies.splice(index, 1);
        }
    }

    save(): Promise<User> {
        const a: Promise<any>[] = [];
        a.push(super.save());
        for (const item of this.studyToDelete) {
            a.push(this.studyService.deleteUserFromStudy(item.id, this.entity.id));
        }
        return Promise.all([a]).then(() => {
            return null;
        });
    }

    changePassword() {
        window.open(KEYCLOAK_BASE_URL + "/realms/shanoir-ng/protocol/openid-connect/auth?client_id=shanoir-ng-front" +
            "&redirect_uri=https%3A%2F%2F" + window.location.hostname + "%2Fshanoir-ng%2Fhome" +
            "&response_mode=fragment" +
            "&response_type=code" +
            "&scope=openid" +
            "&kc_action=UPDATE_PASSWORD", "_self");
    }

    private usualCharsValidator(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            if(control.value != undefined){
                return /^[a-zA-Z\s-]+$/.test(control.value) ? null : { invalidName: true };
            }
            return null;
        };
    }
}
