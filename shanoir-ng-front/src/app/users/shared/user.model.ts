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
    firstName: string;
    lastLogin: Date;
    lastLoginOn: Date;
    lastName: string;
    medical: boolean;
    motivation: string;
    role: Role;
    username: string;
    valid: boolean;
}