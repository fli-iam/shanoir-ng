import { AccountRequestInfo } from '../account-request-info/account-request-info.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { ExtensionRequestInfo } from '../extension-request/extension-request-info.model';
import { Role } from '../../roles/role.model';
import { ServiceLocator } from '../../utils/locator.service';
import { UserService } from './user.service';

export class User extends Entity{
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