export class User {
    id: number;
    username: string;
    firstName: string;
    lastName: string;
    email: string;
    team: string;
    role: string;
    canAccessToDicomAssociation: boolean;
    createdOn: Date;
    expirationDate: Date;
    valid: boolean;
    lastLoginOn: Date;
}