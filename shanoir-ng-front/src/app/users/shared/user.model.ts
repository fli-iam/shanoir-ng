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

import { AccountRequestInfo } from '../account-request-info/account-request-info.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { ExtensionRequestInfo } from '../extension-request/extension-request-info.model';
import { Role } from '../../roles/role.model';
import { ServiceLocator } from 'src/app/utils/locator.service';
import { UserService } from "./user.service";

export class User extends Entity {
    id: number;
    accountRequestDemand: boolean;
    accountRequestInfo: AccountRequestInfo;
    canAccessToDicomAssociation: boolean;
    creationDate: Date;
    email: string;
    expirationDate: Date;
    extensionRequestInfo: ExtensionRequestInfo;
    extensionRequestDemand: boolean;
    firstName: string;
    lastLogin: Date;
    lastLoginOn: Date;
    lastName: string;
    motivation: string;
    onDemand: boolean;
    role: Role;
    username: string;
    valid: boolean;
    selected: boolean = false;

    service: UserService = ServiceLocator.injector.get(UserService);
}