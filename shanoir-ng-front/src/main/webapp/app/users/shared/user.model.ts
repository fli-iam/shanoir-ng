import { Role } from '../../roles/role.model';

export class User {
    id: number;
    canAccessToDicomAssociation: boolean;
    creationDate: Date;
    email: string;
    expirationDate: Date;
    firstName: string;
    lastLogin: Date;
    lastName: string;
    role: Role;
    teamName: string;
    username: string;
    valid: boolean;
    lastLoginOn: Date;
    isMedical: boolean;
    motivation: string;
}