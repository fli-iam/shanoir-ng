import { AccountRequestInfo } from '../account-request-info/account-request-info.model';
import { ExtensionRequestInfo } from '../extension-request/extension-request-info.model';
import { Role } from '../../roles/role.model';

export class User {
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
}