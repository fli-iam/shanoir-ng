import { Role } from "../../roles/role.model";
import { StudyUserType } from "./study-user-type.enum";

export class StudyUser {
    studyId: number;
    userId: number;
    receiveAnonymizationReport: boolean;
    receiveNewImportReport: boolean;
    _studyUserType: any;
    userName: string;
    firstName: string;
    lastName: string;
    email: string;
    role: Role;

    get studyUserType(): any {
        if (typeof this._studyUserType == 'string') {
            this._studyUserType = StudyUserType.get(this._studyUserType);
        }
        return this._studyUserType;
    }

    set studyUserType(studyUserType: any) {
        this._studyUserType = studyUserType;
    }
}