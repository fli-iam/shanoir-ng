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
            .then(roles => {
                this.roles = roles;
                this.getUser();
            })
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
            .subscribe((user: User) => {
                user.role = this.getRoleById(user.role.id);
                this.user = user;
            });
    }

    cancel(): void {
        this.router.navigate(['../userlist']);
    }

    accept(): void {
        this.userService.confirmAccountRequest(this.userId, this.user)
           .subscribe((user) => {
                this.cancel();
             }, (err: String) => {
                if (err.indexOf("username should be unique") != -1) {
                    this.isUserNameUnique = false;
                }
                if (err.indexOf("email should be unique") != -1) {
                    this.isEmailUnique = false;
                }
           });
    }

    deny(): void {
        this.userService.denyAccountRequest(this.userId)
            .then()
                .catch((error) => {
                // TODO: display error
                log.error("error deny account request!");
        });
    }

    create(): void {
        this.userService.create(this.user)
            .subscribe((user) => {
                this.cancel();
            }, (err: String) => {
                if (err.indexOf("username should be unique") != -1) {
                    this.isUserNameUnique = false;
                }
                if (err.indexOf("email should be unique") != -1) {
                    this.isEmailUnique = false;
                }
            });
    }

    update(): void {
        this.userService.update(this.userId, this.user)
           .subscribe((user) => {
                this.cancel();
            }, (err: String) => {
                if (err.indexOf("username should be unique") != -1) {
                    this.isUserNameUnique = false;
                }
                if (err.indexOf("email should be unique") != -1) {
                    this.isEmailUnique = false;
                }
            });
    }

    submit(): void {
        this.user = this.editUserForm.value;
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

    get expirationDate(): String {
        if (this.user && this.user.expirationDate && !isNaN(new Date(this.user.expirationDate).getTime())) {
            return new Date(this.user.expirationDate).toISOString().split('T')[0];
        }
        return "";
    }

    set expirationDate(dateStr: String) {
        this.user.expirationDate = new Date(dateStr);
    }

    getRoleById(id:number): Role {
        for (let role of this.roles) {
            if (id == role.id) {
                return role;
            }
        }
        return null;
    }
    
}