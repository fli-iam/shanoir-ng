import { Role } from "../../roles/role.model";

export class StudyUser {
    studyId: number;
    userId: number;
    receiveAnonymizationReport: boolean;
    receiveNewImportReport: boolean;
    studyUserType: number;
    userName: string;
    firstName: string;
    lastName: string;
    email: string;
    role: Role;
}