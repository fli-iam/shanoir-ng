export class StudyUserType {

    public static RESPONSIBLE = new StudyUserType("Is responsible for the research study");
    public static SEE_DOWNLOAD_IMPORT_MODIFY = new StudyUserType("Can see, download, import datasets and modify the study parameters");
    public static SEE_DOWNLOAD_IMPORT = new StudyUserType("Can see, download and import datasets");
    public static NOT_SEE_DOWNLOAD = new StudyUserType("Cannot see or download datasets");
    public static SEE_DOWNLOAD = new StudyUserType("Can see and download datasets");

    public _value: string;
    
    private constructor(public label: string) {}

    private get value(): string {
        if (!this._value) {
            for (let prop in StudyUserType) {
                if (this.label == StudyUserType[prop].label) {
                    this._value = prop;
                    break;
                }
            }
        }
        return this._value;
    }

    public static all(): StudyUserType[] {
        let all: StudyUserType[] = [];
        for (let prop in StudyUserType) {
            all.push(StudyUserType[prop]);
        }
        return all;
    }

    public static get(name: string): StudyUserType {
        for (let studyUserType of StudyUserType.all()) {
            if (studyUserType.value == name) return studyUserType;
        } 
    }
}