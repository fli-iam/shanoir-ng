import { allOfEnum } from '../../utils/app.utils';

export enum StudyUserType {

    RESPONSIBLE = "RESPONSIBLE",
    SEE_DOWNLOAD_IMPORT_MODIFY = 'SEE_DOWNLOAD_IMPORT_MODIFY',
    SEE_DOWNLOAD_IMPORT = 'SEE_DOWNLOAD_IMPORT',
    NOT_SEE_DOWNLOAD = 'NOT_SEE_DOWNLOAD',
    SEE_DOWNLOAD = 'SEE_DOWNLOAD'

} export namespace StudyUserType {
    
    const allStudyUserTypes: any[] = [
        { value: StudyUserType.NOT_SEE_DOWNLOAD, label: "Cannot see or download datasets" },
        { value: StudyUserType.RESPONSIBLE, label: "Is responsible for the research study" },
        { value: StudyUserType.SEE_DOWNLOAD, label: "Can see and download datasets" },
        { value: StudyUserType.SEE_DOWNLOAD_IMPORT, label: "Can see, download and import datasets" },
        { value: StudyUserType.SEE_DOWNLOAD_IMPORT_MODIFY, label: "Can see, download, import datasets and modify the study parameters" },
    ];
    
    export function all(): Array<StudyUserType> {
        return allOfEnum<StudyUserType>(StudyUserType);
    }

    export function getLabel(type: StudyUserType) {
        let founded = allStudyUserTypes.find(entry => entry.value == type);
        return founded ? founded.label : undefined;
    }

    export function getValueLabelJsonArray() {
        return allStudyUserTypes;
    }
}