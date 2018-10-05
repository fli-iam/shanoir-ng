import { Entity } from '../../shared/components/entity/entity.abstract';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { StudyCard } from '../../study-cards/shared/study-card.model';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { User } from '../../users/shared/user.model';
import { ServiceLocator } from '../../utils/locator.service';
import { MembersCategory } from './members-category.model';
import { StudyCenter } from './study-center.model';
import { StudyType } from './study-type.enum';
import { StudyUser } from './study-user.model';
import { StudyService } from './study.service';
import { Timepoint } from './timepoint.model';

export class Study extends Entity {
    clinical: boolean;
    compatible: boolean;
    downloadableByDefault: boolean;
    endDate: Date;
    experimentalGroupsOfSubjects: IdNameObject[];
    id: number;
    membersCategories: MembersCategory[];
    monoCenter: boolean;
    name: string;
    nbExaminations: number;
    nbSujects: number;
    protocolFilePathList: string[];
    startDate: Date;
    studyCards: StudyCard[];
    studyCenterList: StudyCenter[];
    studyStatus: 'IN_PROGRESS' | 'FINISHED'  = 'IN_PROGRESS';
    studyType: StudyType;
    subjectStudyList: SubjectStudy[] = [];
    studyUserList: StudyUser[] = [];
    timepoints: Timepoint[];
    visibleByDefault: boolean;
    withExamination: boolean;
    selected: boolean = false;
    
    private completeMembers(users: User[]) {
        return Study.completeMembers(this, users);
    }
    
    public static completeMembers(study: Study, users: User[]) {
        if (!study.studyUserList) return;
        for (let studyUser of study.studyUserList) {
            StudyUser.completeMember(studyUser, users); 
        }
    }

    protected getIgnoreList(): string[] {
        return super.getIgnoreList().concat(['completeMembers']);
    }
    
    service: StudyService = ServiceLocator.injector.get(StudyService);
}