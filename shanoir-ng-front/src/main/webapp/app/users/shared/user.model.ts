import { Role } from '../../roles/role.model';

export class User {
    id: number;
    accountRequestDemand: boolean;
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
    teamName: string;
    username: string;
    valid: boolean;
}