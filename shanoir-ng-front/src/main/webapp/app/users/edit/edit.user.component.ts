import { Component, OnInit } from '@angular/core';
import { Router }  from '@angular/router';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import {User} from '../shared/user.model';
import {UserService} from '../shared/user.service';
import {Role} from '../../roles/role.model';
import {RoleService} from '../../roles/role.service';

@Component({
    selector: 'editUser',
    moduleId: module.id,
    templateUrl: 'edit.user.component.html',
    styleUrls: ['../../shared/css/common.css', 'edit.user.component.css']
})

export class EditUserComponent implements OnInit {
    users: User[];
    user: User;
    router: Router;
    userService: UserService;
    editUserForm: FormGroup;
    roles: Role[];
    roleService: RoleService;

    constructor(router: Router, userService: UserService, roleService: RoleService, fb:FormBuilder) {
        this.router = router;
        this.userService = userService;
        this.roleService = roleService;
        this.editUserForm = fb.group({
            'firstName': ['', Validators.required, Validators.minLength(2), Validators.maxLength(50)],
            'lastName': ['', Validators.required, Validators.minLength(2), Validators.maxLength(50)],
            'username': ['', Validators.required, Validators.minLength(4), Validators.maxLength(20)],
            'email': ['', Validators.required],
            'expirationDate': '',
            'role': ['', Validators.required],
            'canAccessToDicomAssociation': new FormControl('false'),
            'isMedical': new FormControl('false'),
            'motivation': ''
        });
    }

    getRoles(): void {
        this.roleService
            .getRoles()
            .then(roles => this.roles = roles);
    }

    cancel (): void {
        this.router.navigate(['../userlist']);
    }
    
    create(): void {
        this.user = new User();
        
        this.user.firstName = this.editUserForm.value.firstName;
        this.user.lastName = this.editUserForm.value.lastName;
        this.user.username = this.editUserForm.value.username;
        this.user.email = this.editUserForm.value.email;
        this.user.expirationDate = this.editUserForm.value.expirationDate;
        this.user.role = this.editUserForm.value.role;
        this.user.canAccessToDicomAssociation = this.editUserForm.value.canAccessToDicomAssociation;
        this.user.isMedical = this.editUserForm.value.isMedical;
        this.user.motivation = this.editUserForm.value.motivation;
        // to be removed
        this.user.creationDate = new Date();

        if (!this.user) {return;}
        this.userService.create(this.user)
            .then(user => {
                this.cancel();
            });
    }

    ngOnInit(): void {
        this.getRoles();
    }
    
}