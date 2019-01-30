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
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { IMyDate } from 'mydatepicker';
import { Role } from '../../roles/role.model';
import { RoleService } from '../../roles/role.service';
import { AccountRequestInfo } from '../account-request-info/account-request-info.model';
import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';

@Component({
    selector: 'user-detail',
    templateUrl: 'user.component.html',
    styleUrls: ['user.component.css']
})

export class UserComponent implements OnInit {
    @Input() requestAccountMode: boolean = false;
    @Output() closing = new EventEmitter();
    user: User = new User();
    userForm: FormGroup;
    roles: Role[];
    isEmailUnique: boolean = true;
    isDateValid: boolean = true;
    selectedDateNormal: IMyDate;
    accountRequestInfo: AccountRequestInfo;
    private accountRequestInfoValid: boolean = false;
    private id: number;
    @Input() mode: "view" | "edit" | "create";

    constructor(private router: Router, private location: Location, private route: ActivatedRoute,
        private userService: UserService, private roleService: RoleService, private fb: FormBuilder) {
            this.mode = this.route.snapshot.data['mode'];
            this.id = +this.route.snapshot.params['id'];
    }

    ngOnInit(): void {
        if (this.requestAccountMode) {
            this.mode = "create";
        } else {
            this.getRoles();
        }
        this.buildForm();
    }

    getRoles(): void {
        this.roleService
            .getRoles()
            .then(roles => {
                this.roles = roles;
                this.getUser();
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting roles list!");
            });
    }

    getUser(): void {
        if (this.mode == 'create') {
            this.user = new User();
        } else {
            this.userService.getUser(this.id).then((user: User) => {
                user.role = this.getRoleById(user.role.id);
                this.user = user;
                if (user.extensionRequestDemand) {
                    this.user.expirationDate = user.extensionRequestInfo.extensionDate;
                }
                this.accountRequestInfo = this.user.accountRequestInfo;
            });
        }
    }

    getOut(user: User = null): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(user);
        } else {
            this.location.back();
        }
    }

    cancelAccountRequest(): void {
        window.location.href = process.env.LOGOUT_REDIRECT_URL;
    }

    accept(): void {
        this.submit();
        this.userService.confirmAccountRequest(this.id, this.user)
            .subscribe((user) => {
                this.getOut();
            }, (err: String) => {
                if (err.indexOf("email should be unique") != -1) {
                    this.isEmailUnique = false;
                }
            });
    }

    deny(): void {
        this.userService.denyAccountRequest(this.id)
            .then(res => {
                this.getOut();
            })
            .catch((error) => {
                // TODO: display error
                //log.error("error deny account request!");
                console.log("error deny account request!");
            });
    }

    create(): void {
        this.submit();
        this.userService.create(this.user)
            .subscribe((user) => {
                this.getOut(user);
            }, (err: String) => {
                if (err.indexOf("email should be unique") != -1) {
                    this.isEmailUnique = false;
                }
            });
    }

    accountRequest(): void {
        this.submit();
        this.userService.requestAccount(this.user)
            .subscribe((res) => {
                this.getOut(res);
            }, (err: String) => {
                if (err.indexOf("email should be unique") != -1) {
                    this.isEmailUnique = false;
                } else {
                    this.getOut();
                }
            });
    }

    update(): void {
        this.submit();
        this.userService.update(this.id, this.user)
            .subscribe((user) => {
                this.getOut(user);
            }, (err: String) => {
                if (err.indexOf("email should be unique") != -1) {
                    this.isEmailUnique = false;
                }
            });
    }

    submit(): void {
        this.user = this.userForm.value;
        this.user.accountRequestInfo = this.accountRequestInfo;
    }

    isUserFormValid(): boolean {
        if (this.userForm.valid && this.isDateValid) {
            if (this.requestAccountMode) {
                if (this.accountRequestInfoValid) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    buildForm(): void {
        const emailRegex = '^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$';
        let roleFC: FormControl;
        if (this.requestAccountMode) {
            roleFC = new FormControl(this.user.role);
        } else {
            roleFC = new FormControl(this.user.role, Validators.required);
        }
        this.userForm = this.fb.group({
            'firstName': [this.user.firstName, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
            'lastName': [this.user.lastName, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
            'username': new FormControl(this.user.username),
            'email': [this.user.email, [Validators.required, Validators.pattern(emailRegex)]],
            'expirationDate': [this.user.expirationDate],
            'extensionMotivation': [(this.user.extensionRequestInfo) ? this.user.extensionRequestInfo.extensionMotivation : ''],
            'role': roleFC,
            'canAccessToDicomAssociation': new FormControl('false')
        });

        this.userForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.userForm) { return; }
        const form = this.userForm;
        for (const field in this.formErrors) {
            // clear previous error message (if any)
            this.formErrors[field] = '';
            const control = form.get(field);
            if (control && control.dirty && !control.valid) {
                for (const key in control.errors) {
                    this.formErrors[field] += key;
                }
            }
        }
    }

    formErrors = {
        'firstName': '',
        'lastName': '',
        'email': '',
        'role': ''
    };

    getRoleById(id: number): Role {
        for (let role of this.roles) {
            if (id == role.id) {
                return role;
            }
        }
        return null;
    }

    saveARI(ari: AccountRequestInfo): void {
        this.accountRequestInfo = ari;
    }

    updateARIValid(ariValid: boolean): void {
        this.accountRequestInfoValid = ariValid;
    }

}