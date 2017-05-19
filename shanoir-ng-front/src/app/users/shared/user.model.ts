import { Role } from '../../roles/role.model';
import { AccountRequestInfo } from '../accountRequestInfo/account.request.info.model';

export class User {
    id: number;
    accountRequestDemand: boolean;
    accountRequestInfo: AccountRequestInfo;
    canAccessToDicomAssociation: boolean;
    creationDate: Date;
    email: string;
    expirationDate: Date;
    extensionDate: Date;
    extensionMotivation: string;
    extensionRequest: boolean;
    firstName: string;
    lastLogin: Date;
    lastLoginOn: Date;
    lastName: string;
    motivation: string;
    onDemand: boolean;
    role: Role;
    username: string;
    valid: boolean;
}