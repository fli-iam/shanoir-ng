import { Injectable } from '@angular/core';

import { User } from './user.model';
import { USERS } from './mock-users';

@Injectable()
export class UserService {
    
    constructor() { }

    getUsers(): Promise<User[]> {
        return Promise.resolve(USERS);
    }
    
}