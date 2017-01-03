import { Role } from '../../roles/role.model';

export class User {
    id: number;
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
    onDemand: boolean;
    role: Role;
    teamName: string;
    username: string;
    valid: boolean;
}