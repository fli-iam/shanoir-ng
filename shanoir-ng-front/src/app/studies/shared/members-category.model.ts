import { IdNameObject } from "../../shared/models/id-name-object.model";
import { StudyUserType } from "./study-user-type.enum";

export class MembersCategory {
    members: IdNameObject[];
    studyUserType: StudyUserType;
}