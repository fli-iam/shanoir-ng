import { Component } from '@angular/core';
import { Location }  from '@angular/common';

import {User} from '../shared/user.model';
import {UserService} from '../shared/user.service';

@Component({
    selector: 'editUser',
    moduleId: module.id,
    templateUrl: 'edit.user.component.html',
    styleUrls: ['../../shared/css/common.css', 'edit.user.component.css']
})

export class EditUserComponent {
    user: User;

    roles = ['', 'Guest', 'User', 'Expert', 'Medical', 'Administrator'];

    constructor(
        private lcoation: Location,
        private userService: UserService
    ) {}

    cancel ():void {
        //this.location.back();    
    }
    
    create(): void {
 
  }
    
}