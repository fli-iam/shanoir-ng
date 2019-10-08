import { Component, ViewChild, ElementRef, OnChanges, Input} from '@angular/core';
import { FormGroup,  Validators } from '@angular/forms';

import { ContrastAgent }    from '../../contrastAgent/shared/contrastAgent.model';
import { ContrastAgentService } from '../../contrastAgent/shared/contrastAgent.service';
import { Examination } from '../../../examinations/shared/examination.model';
import { ExaminationAnesthetic }    from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.model';
import { ExaminationAnestheticService }    from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.service';
import { AnimalExaminationService } from '../shared/animal-examination.service';
import { ExtraData }    from '../../extraData/extraData/shared/extradata.model';
import { BloodGasData }    from '../../extraData/bloodGasData/shared/bloodGasData.model';
import { BloodGasDataFile }    from '../../extraData/bloodGasData/shared/bloodGasDataFile.model';
import { PhysiologicalData }    from '../../extraData/physiologicalData/shared/physiologicalData.model';
import { PhysiologicalDataFile }    from '../../extraData/physiologicalData/shared/physiologicalDataFile.model';
import { ExtraDataService } from '../../extraData/extraData/shared/extradata.service';
import { CenterService } from '../../../centers/shared/center.service';
import { StudyService } from '../../../studies/shared/study.service';
import { IdName } from '../../../shared/models/id-name.model';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { Subject } from '../../../subjects/shared/subject.model';
import { AnimalSubjectService } from '../../animalSubject/shared/animalSubject.service';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { ModesAware } from "../../shared/mode/mode.decorator";
import { EntityComponent } from '../../../shared/components/entity/entity.component.abstract';
import { ActivatedRoute } from '@angular/router';
import { DatepickerComponent } from '../../../shared/date-picker/date-picker.component';
import { BreadcrumbsService } from '../../../breadcrumbs/breadcrumbs.service';
import { SubjectWithSubjectStudy } from '../../../subjects/shared/subject.with.subject-study.model';

@Component({
    selector: 'examination-preclinical-form',
    templateUrl: 'animal-examination-form.component.html',
    providers: [ExtraDataService, ContrastAgentService, ExaminationAnestheticService, AnimalExaminationService],
    styleUrls: ['animal-examination.component.css']
})
@ModesAware
export class AnimalExaminationFormComponent extends EntityComponent<Examination>{

	@ViewChild('instAssessmentModal') instAssessmentModal: ModalComponent;
    @ViewChild('attachNewFilesModal') attachNewFilesModal: ModalComponent;
    
    urlupload: string;
    physioData: PhysiologicalData;
    examinationPhysioData: PhysiologicalData = new PhysiologicalData();
    physioDataFile: PhysiologicalDataFile;
    bloodGasData: BloodGasData;
    examinationBloodGasData: BloodGasData = new BloodGasData();
    bloodGasDataFile: BloodGasDataFile;
    extraData: ExtraData;
    contrastAgent: ContrastAgent = new ContrastAgent();
    examAnesthetic: ExaminationAnesthetic = new ExaminationAnesthetic();
    examinationExtradatas: ExtraData[] = [];
    centers: IdName[] = [];
    studies: IdName[] = [];
    private subjects: SubjectWithSubjectStudy[];
    animalSubjectId: number;
    private inImport: boolean; 
    
    constructor(
        private route: ActivatedRoute,
        private animalExaminationService: AnimalExaminationService, 
        private examAnestheticService: ExaminationAnestheticService,
        private extradatasService: ExtraDataService,
        private contrastAgentsService: ContrastAgentService,
        private animalSubjectService: AnimalSubjectService, 
        private centerService: CenterService,
        private studyService: StudyService, 
        protected breadcrumbsService: BreadcrumbsService) 
    {

        super(route, 'preclinical-examination');
        this.inImport = breadcrumbsService.isImporting();
        this.manageSaveEntity();
    }
    
    get examination(): Examination { return this.entity; }
    set examination(examination: Examination) { this.entity = examination; }

    initView(): Promise<void> {
        return this.animalExaminationService.get(this.id).then(examination => {
            this.examination = examination; 
            this.updateExam();
            //this.loadExaminationAnesthetic();
            if(this.examination && this.examination.subject && this.examination.subject.id ){
                this.animalSubjectService
        			.findAnimalSubjectBySubjectId(this.examination.subject.id)
        			.then(animalSubject => this.animalSubjectId = animalSubject.id)
                    .catch((error) => {});
                
        	}
        });
    }

    
    initEdit(): Promise<void> {
        this.getCenters();
        this.getStudies();
        return this.animalExaminationService.get(this.id).then(examination => {
            this.examination = examination;
            this.updateExam();
            //this.loadExaminationAnesthetic(this.id);
            if(this.examination && this.examination.subject && this.examination.subject.id){
                this.animalSubjectService
        			.findAnimalSubjectBySubjectId(this.examination.subject.id)
        			.then(animalSubject => this.animalSubjectId = animalSubject.id)
                    .catch((error) => {});
                
        	}
        });

    }

    initCreate(): Promise<void> {
        this.entity = new Examination();
        this.examination.preclinical = true;
        this.getCenters();
        this.getStudies();
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'study': [{value: this.examination.study, disabled: this.inImport}, Validators.required],
            'subject': [{value: this.examination.subject, disabled: this.inImport}],
            'center': [{value: this.examination.center, disabled: this.inImport}, Validators.required],
            'examinationDate': [this.examination.examinationDate, [Validators.required, DatepickerComponent.validator]],
            'comment': [this.examination.comment],
            'note': [this.examination.note],
            'subjectWeight': [this.examination.subjectWeight]
        });
    }

    private instAssessment() {
    }

    private attachNewFiles() {
    }

    private updateExam(): void{
        this.examination.subjectStudy = new SubjectWithSubjectStudy();
        if (this.examination.subject){
            this.examination.subjectStudy.id = this.examination.subject.id;
            this.examination.subjectStudy.name = this.examination.subject.name;
        }
    }

    private updateExamForSave(): void{
        this.examination.centerId = this.examination.center.id;
        this.examination.studyId = this.examination.study.id;
        this.examination.subjectId = this.examination.subject.id;
    }
    
    private getCenters(): void {
        this.centers = [];
        this.centerService
            .getCentersNamesForExamination()
            .then(centers => {
                this.centers = centers;
            });
    }

    private getStudies(): void {
        this.studies = [];
        this.studyService
            .getStudiesNames()
            .then(studies => {
                this.studies = studies;
            });
    }
    
    
    private getSubjects(): void {
        if (!this.examination.study) return;
        this.studyService
            .findSubjectsByStudyId(this.examination.study.id)
            .then(subjects => this.subjects = subjects);
    }


    manageSaveEntity(): void {
        this.subscribtions.push(
            this.onSave.subscribe(response => {
                this.manageExaminationAnesthetic(response.id);
                //this.manageContrastAgent();
                this.addExtraDataToExamination(response.id, false);
            })
        );
       
    }


    protected save(): Promise<void> {
        this.updateExamForSave();
        return super.save();
    }

    manageExaminationAnesthetic(examination_id : number) {
        if (this.examAnesthetic) {
            this.examAnesthetic.examination_id = examination_id;
            if (this.examAnesthetic  && this.examAnesthetic.internal_id) {
                this.examAnestheticService.updateAnesthetic(examination_id, this.examAnesthetic)
                    .then(examAnesthetic => {
                    });
            } else if (this.examAnesthetic.anesthetic ){
                this.examAnestheticService.createAnesthetic(examination_id, this.examAnesthetic)
                    .then(examAnesthetic => {
                    });
            }
        }
    }

  /*  manageContrastAgent() {
        if (this.protocol_id && this.contrastAgent) {
            if (this.contrastAgent.id) {
                this.contrastAgentsService.update(this.protocol_id, this.contrastAgent)
                    .subscribe(agent => {
                    });
            } else {
                this.contrastAgentsService.create(this.protocol_id, this.contrastAgent)
                    .subscribe(agent => {
                    });
            }
        }
    }*/

    addExtraDataToExamination(examination_id: number, isUpdate: boolean) {
        if (!examination_id) { return; }
        //Set the upload URL model
        let uploadUrl: string = PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL + "/" + PreclinicalUtils.PRECLINICAL_EXTRA_DATA + PreclinicalUtils.PRECLINICAL_UPLOAD_URL + "/";
        if (this.physioData) {
            this.physioData.examination_id = examination_id;
            //Create physio data
            if (isUpdate && this.examinationPhysioData && this.examinationPhysioData.id){
            	this.extradatasService.updateExtradata(PreclinicalUtils.PRECLINICAL_PHYSIO_DATA,this.examinationPhysioData.id, this.physioData)
                .subscribe(physioData => {
                	if (this.physioDataFile.physiologicalDataFile){
                    this.extradatasService.postFile(this.physioDataFile.physiologicalDataFile, physioData)
                    	.subscribe(res => {
                    		this.examinationExtradatas.push(physioData);
                    	}, (err: String) => {
                    		console.log('error in posting File ' + err);
                    	});
                    }
                    //Add extra data to array
                    this.examinationExtradatas.push(physioData);
                });
            }else{
            	this.extradatasService.createExtraData(PreclinicalUtils.PRECLINICAL_PHYSIO_DATA, this.physioData)
                .subscribe(physioData => {
                if (this.physioDataFile.physiologicalDataFile){
                    this.extradatasService.postFile(this.physioDataFile.physiologicalDataFile, physioData)
                    	.subscribe(res => {
                    		this.examinationExtradatas.push(physioData);
                    	}, (err: String) => {
                    		console.log('error in posting File ' + err);
                    	});
                    }
                    //Add extra data to array
                    this.examinationExtradatas.push(physioData);
                });
            }
        }
        if (this.bloodGasData) {
            this.bloodGasData.examination_id = examination_id;
            //Create blood gas data
             if (isUpdate && this.examinationBloodGasData && this.examinationBloodGasData.id){
            	this.extradatasService.updateExtradata(PreclinicalUtils.PRECLINICAL_BLOODGAS_DATA, this.examinationBloodGasData.id, this.bloodGasData)
                	.subscribe(bloodGasData => {
                	if (this.bloodGasDataFile.bloodGasDataFile){
                    	this.extradatasService.postFile(this.bloodGasDataFile.bloodGasDataFile, bloodGasData)
                    		.subscribe(res => {
                    			this.examinationExtradatas.push(bloodGasData);
                    		}, (err: String) => {
                    			console.log('error in posting File ' + err);
                    		});
                    }
                    this.examinationExtradatas.push(bloodGasData);
                });
            }else{
            	this.extradatasService.createExtraData(PreclinicalUtils.PRECLINICAL_BLOODGAS_DATA, this.bloodGasData)
                	.subscribe(bloodGasData => {
                	if (this.bloodGasDataFile.bloodGasDataFile){
                    	this.extradatasService.postFile(this.bloodGasDataFile.bloodGasDataFile, bloodGasData)
                    		.subscribe(res => {
                    			this.examinationExtradatas.push(bloodGasData);
                    		}, (err: String) => {
                    			console.log('error in posting File ' + err);
                    		});
                    }
                    this.examinationExtradatas.push(bloodGasData);
                });
            }
        }
    }

    onUploadExtraData(event) {
        this.extraData = event;
        this.extraData.extradatatype = "Extra data"
        this.examinationExtradatas.push(this.extraData);
        //this.examinationExtradatas = null;
    }

    onUploadPhysiologicalData(event) {
        this.physioDataFile = event;
        this.physioData = new PhysiologicalData();
        this.physioData.filename =  this.physioDataFile.filename;
        this.physioData.extradatatype = "Physiological data";
        this.physioData.has_heart_rate = this.physioDataFile.has_heart_rate;
        this.physioData.has_respiratory_rate = this.physioDataFile.has_respiratory_rate;
        this.physioData.has_sao2 = this.physioDataFile.has_sao2;
        this.physioData.has_temperature = this.physioDataFile.has_temperature;
        
    }

    onUploadBloodGasData(event) {
        this.bloodGasDataFile = event;
        this.bloodGasData = new BloodGasData();
        this.bloodGasData.filename =  this.bloodGasDataFile.filename;
        this.bloodGasData.extradatatype = "Blood gas data"
    }
    
    onExamAnestheticChange(event) {
        this.examAnesthetic = event;
    }
    
    onAgentChange(event) {
        this.contrastAgent = event;
    }
    
    
    closePopin(instAssessmentId?: number) {
        this.instAssessmentModal.hide();
    }

    closeAttachedFilePopin(id?: number) {
        this.attachNewFilesModal.hide();
    }
    

}