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
import { StudyUser } from '../../studies/shared/study-user.model';
import { Field } from '../../shared/reflect/field.decorator';

export class User extends Entity {
    @Field() id: number;
    @Field() accountRequestDemand: boolean;
    @Field() accountRequestInfo: AccountRequestInfo;
    @Field() canAccessToDicomAssociation: boolean;
    @Field() creationDate: Date;
    @Field() email: string;
    @Field() expirationDate: Date;
    @Field() extensionRequestInfo: ExtensionRequestInfo;
    @Field() extensionRequestDemand: boolean;
    @Field() firstName: string;
    @Field() lastLogin: Date;
    @Field() lastLoginOn: Date;
    @Field() lastName: string;
    @Field() motivation: string;
    @Field() onDemand: boolean;
    @Field() role: Role;
    @Field() username: string;
    @Field() valid: boolean;
    @Field() selected: boolean = false;
    @Field() studyUserList: StudyUser[];
}