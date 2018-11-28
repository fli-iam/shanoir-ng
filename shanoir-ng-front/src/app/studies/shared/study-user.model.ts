import { User } from "../../users/shared/user.model";
import { StudyUserType } from "./study-user-type.enum";

export class StudyUser {
    studyId: number;
    userId: number;
    receiveAnonymizationReport: boolean;
    receiveNewImportReport: boolean;
    studyUserType: StudyUserType;
    userName: string;
    user: User;

    public completeMember(users: User[]) {
        StudyUser.completeMember(this, users);
    }

    public static completeMember(studyUser: StudyUser, users: User[]) {
        for (let user of users) {
            if (studyUser.userId == user.id) {
                studyUser.user = user;
                user.selected = true;
            }
        }
    }
}