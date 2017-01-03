import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
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
    route: ActivatedRoute;
    userService: UserService;
    editUserForm: FormGroup;
    roles: Role[];
    roleService: RoleService;
    isUserNameUnique: Boolean = true;
    isEmailUnique: Boolean = true;
    creationMode: Boolean;
    selectedRole: Role = new Role();
    userId: number;

    constructor(router: Router, route: ActivatedRoute, userService: UserService, roleService: RoleService, private fb: FormBuilder) {
        this.router = router;
        this.route = route;
        this.userService = userService;
        this.roleService = roleService;
    }

    getRoles(): void {
        this.roleService
            .getRoles()
            .then(roles => this.roles = roles)
            .catch((error) => {
            // TODO: display error
            log.error("error getting roles list!");
        });
    }

    getUser(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let userId = queryParams['id'];
                if (userId) {
                    this.creationMode = false;
                    this.userId = userId;
                    return this.userService.getUser(userId);
                } else { 
                    this.creationMode = true;
                    return Observable.of<User>();
                }
            })
            .subscribe(user => {
               this.user = user;
               this.selectedRole = user.role;
            });
    }

    cancel(): void {
        this.router.navigate(['../userlist']);
    }

    submit(): void {
        this.user = this.editUserForm.value;
        if (this.creationMode == true) {
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
        } else {
            this.userService.update(this.userId, this.user)
                .then((user) => {
                    this.user = user;
                    this.cancel();
                })
                .catch((error) => {
                    // TODO: display error
                    console.error("error updating user!");
                });
        }
    }

    ngOnInit(): void {
        this.getUser();
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

    get expirationDate(): String {
        if (this.user && this.user.expirationDate && !isNaN(new Date(this.user.expirationDate).getTime())) {
            return new Date(this.user.expirationDate).toISOString().split('T')[0];
        }
        return "";
    }

    set expirationDate(dateStr: String) {
        this.user.expirationDate = new Date(dateStr);
    }
    
}