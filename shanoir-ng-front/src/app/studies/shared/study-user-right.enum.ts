import { allOfEnum } from '../../utils/app.utils';

export enum StudyUserRight {

    CAN_SEE_ALL = "CAN_SEE_ALL",
    CAN_DOWNLOAD = "CAN_DOWNLOAD",
    CAN_IMPORT = "CAN_IMPORT",
    CAN_ADMINISTRATE = "CAN_ADMINISTRATE"

} export namespace StudyUserRight {
    
    const allStudyUserRights: any[] = [
        { value: StudyUserRight.CAN_SEE_ALL, label: "Can see all data in this study" },
        { value: StudyUserRight.CAN_DOWNLOAD, label: "Can download datasets from this study" },
        { value: StudyUserRight.CAN_IMPORT, label: "Can import datasets in this study" },
        { value: StudyUserRight.CAN_ADMINISTRATE, label: "Can edit the study parameters" },
    ];
    
    export function all(): Array<StudyUserRight> {
        return allOfEnum<StudyUserRight>(StudyUserRight);
    }

    export function getLabel(type: StudyUserRight) {
        let founded = allStudyUserRights.find(entry => entry.value == type);
        return founded ? founded.label : undefined;
    }

    export function getValueLabelJsonArray() {
        return allStudyUserRights;
    }
}