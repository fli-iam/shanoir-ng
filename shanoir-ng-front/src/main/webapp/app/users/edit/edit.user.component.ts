import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Observable } from 'rxjs/Observable';

import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';
import { Role } from '../../roles/role.model';
import { RoleService } from '../../roles/role.service';

@Component({
    selector: 'editUser',
    moduleId: module.id,
    templateUrl: 'edit.user.component.html',
    styleUrls: ['../../shared/css/common.css', 'edit.user.component.css']
})

export class EditUserComponent implements OnInit {
    user: User = new User();
    router: Router;
    userService: UserService;
    editUserForm: FormGroup;
    roles: Role[];
    roleService: RoleService;
    isUserNameUnique: Boolean = true;
    isEmailUnique: Boolean = true;

    constructor(router: Router, userService: UserService, roleService: RoleService, private fb: FormBuilder) {
        this.router = router;
        this.userService = userService;
        this.roleService = roleService;
    }

    getRoles(): void {
        this.roleService
            .getRoles()
            .then(roles => this.roles = roles);
    }

    cancel(): void {
        this.router.navigate(['../userlist']);
    }

    create(): void {
        this.user = this.editUserForm.value;

        this.userService.create(this.user)
            .subscribe((user) => {
                this.cancel();
            }, (err: String) => {
                if (err.indexOf("username unique") != -1) {
                    this.isUserNameUnique = false;
                }
                if (err.indexOf("email unique") != -1) {
                    this.isEmailUnique = false;
                }
            });
    }

    ngOnInit(): void {
        this.getRoles();
        this.buildForm();
    }

    buildForm(): void {
        const emailRegex = '^[a-z0-9]+(\.[_a-z0-9]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,15})$';
        this.editUserForm = this.fb.group({
            'firstName': [this.user.firstName, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
            'lastName': [this.user.lastName, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
            'username': [this.user.username, [Validators.required, Validators.minLength(4), Validators.maxLength(20)]],
            'email': [this.user.email, [Validators.required, Validators.pattern(emailRegex)]],
            'expirationDate': [this.user.expirationDate],
            'role': [this.user.role, Validators.required],
            'canAccessToDicomAssociation': new FormControl('false'),
            'isMedical': new FormControl('false'),
            'motivation': [this.user.motivation]
        });

        this.editUserForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.editUserForm) { return; }
        const form = this.editUserForm;
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
        'username': '',
        'email': '',
        'role': ''
    };
}