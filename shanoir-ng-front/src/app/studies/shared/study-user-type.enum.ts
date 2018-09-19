import { allOfEnum } from '../../utils/app.utils';

export enum StudyUserType {

    RESPONSIBLE = "RESPONSIBLE",
    SEE_DOWNLOAD_IMPORT_MODIFY = 'SEE_DOWNLOAD_IMPORT_MODIFY',
    SEE_DOWNLOAD_IMPORT = 'SEE_DOWNLOAD_IMPORT',
    NOT_SEE_DOWNLOAD = 'NOT_SEE_DOWNLOAD',
    SEE_DOWNLOAD = 'SEE_DOWNLOAD'

} export namespace StudyUserType {

    export function all(): Array<StudyUserType> {
        return allOfEnum<StudyUserType>(StudyUserType);
    }
}