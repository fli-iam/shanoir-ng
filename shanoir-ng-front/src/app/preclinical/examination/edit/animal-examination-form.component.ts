import { Component, OnInit, Input,  Output, EventEmitter, ViewChild, ElementRef, OnChanges, ChangeDetectorRef , SimpleChanges} from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Location } from '@angular/common';

import { ContrastAgent }    from '../../contrastAgent/shared/contrastAgent.model';
import { ContrastAgentService } from '../../contrastAgent/shared/contrastAgent.service';
import { Examination } from '../../../examinations/shared/examination.model';
import { ExaminationAnesthetic }    from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.model';
import { ExaminationAnestheticService }    from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.service';
import { AnimalExaminationService } from '../shared/animal-examination.service';
import { ExtraData }    from '../../extraData/extraData/shared/extradata.model';
import { ExtraDataListComponent } from '../../extraData/extraData/list/extradata-list.component';
import { BloodGasData }    from '../../extraData/bloodGasData/shared/bloodGasData.model';
import { BloodGasDataFile }    from '../../extraData/bloodGasData/shared/bloodGasDataFile.model';
import { PhysiologicalData }    from '../../extraData/physiologicalData/shared/physiologicalData.model';
import { PhysiologicalDataFile }    from '../../extraData/physiologicalData/shared/physiologicalDataFile.model';
import { ExaminationExtraDataService } from '../../extraData/extraData/shared/extradata.service';
import { CenterService } from '../../../centers/shared/center.service';
import { StudyService } from '../../../studies/shared/study.service';
import { IdNameObject } from '../../../shared/models/id-name-object.model';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { Subject } from '../../../subjects/shared/subject.model';
import { AnimalSubjectService } from '../../animalSubject/shared/animalSubject.service';

import * as PreclinicalUtils from '../../utils/preclinical.utils';


import { IMyDate, IMyDateModel, IMyInputFieldChanged, IMyOptions } from 'mydatepicker';

import { Mode } from "../../shared/mode/mode.model";
import { ModesAware } from "../../shared/mode/mode.decorator";
import { EntityComponent } from '../../../shared/components/entity/entity.component.abstract';

@Component({
    selector: 'examination-preclinical-form',
    templateUrl: 'animal-examination-form.component.html',
    providers: [ExaminationExtraDataService, ContrastAgentService, ExaminationAnestheticService, AnimalExaminationService],
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
    centers: IdNameObject[] = [];
    studies: IdNameObject[] = [];
    subjects: Subject[] = [];
    hasStudyCenterData : boolean = false;
    animalSubjectId: number;
    selectedSubjectId: number;
    
    constructor(
        private route: ActivatedRoute,
        private animalExaminationService: AnimalExaminationService, 
        private examAnestheticService: ExaminationAnestheticService,
        private extradatasService: ExaminationExtraDataService,
        private contrastAgentsService: ContrastAgentService,
        private animalSubjectService: AnimalSubjectService, 
        private centerService: CenterService,
        private studyService: StudyService) 
    {

        super(route, 'preclinical-examination');
        this.manageSaveEntity();
    }
    
    get examination(): Examination { return this.entity; }
    set examination(examination: Examination) { this.entity = examination; }

    initView(): Promise<void> {
        this.getSubjects();
        return this.animalExaminationService.get(this.id).then(examination => {
            this.examination = examination; 
            this.setSelectedSubject();
            //this.loadExaminationAnesthetic();
            this.loadExtraDatas();
        });
    }

    
    initEdit(): Promise<void> {
        this.getCenters();
        this.getStudies();
        this.getSubjects();
        return this.animalExaminationService.get(this.id).then(examination => {
            this.examination = examination;
            //this.loadExaminationAnesthetic(this.id);
            this.setSelectedSubject();
            if(this.examination && this.examination.subjectId ){
                this.animalSubjectService
        			.findAnimalSubjectBySubjectId(this.examination.subjectId)
        			.then(animalSubject => this.animalSubjectId = animalSubject.id)
                    .catch((error) => {});
                
        	}
            this.loadExtraDatas();
        });

    }

    initCreate(): Promise<void> {
        this.entity = new Examination();
        this.examination.preclinical = true;
        this.getCenters();
        this.getStudies();
        this.getSubjects();
        return Promise.resolve();
    }
    

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'id': [this.examination.id],
            'studyId': [this.examination.studyId, Validators.required],
            // 'Examination executive': [this.examination.examinationExecutive],
            'centerId': [this.examination.centerId, Validators.required],
            'subject': [this.selectedSubjectId, Validators.required],
            'examinationDate': [this.examination.examinationDate, Validators.required],
            'comment': [this.examination.comment],
            'note': [this.examination.note],
            'subjectWeight': [this.examination.subjectWeight], 
             //regarding examination anesthetic
            newExamAnestheticForm: this.formBuilder.group({
                'anesthetic': [this.examAnesthetic.anesthetic],
                'injectionInterval': [this.examAnesthetic.injection_interval],
                'injectionSite': [this.examAnesthetic.injection_site],
                'injectionType': [this.examAnesthetic.injection_type],
                'dose': [this.examAnesthetic.dose],
                'dose_unit': [this.examAnesthetic.dose_unit]
               // 'startDate': [this.examAnesthetic.startDate],
               // 'endDate': [this.examAnesthetic.endDate]
            }), 
        });
    }

    private instAssessment() {
    }

    private attachNewFiles() {
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
    
    
    
    getSubjects(): void{
    	this.subjects = [];
    	this.animalSubjectService.getPreclinicalSubjects(true)
    		.then(subjects => {
    			this.subjects = subjects;
    		})
    		.catch((error) => {
                // TODO: display error
                console.log("error getting subjects list!");
            });
    }


	
    
    setSelectedSubject(){
        if (this.examination && this.examination.subject){
            this.selectedSubjectId = this.examination.subject.id;
        }
    }
    
    getSubjectById(id: number): Subject{
    	if (this.subjects){
    		for (let s of this.subjects) {
    			if (s.id === id){
    				return s;
    			}
    		}
    	}
    	return null;
    }
    
    updateSubject(){
    	if (this.selectedSubjectId){
    		let subject : Subject = this.getSubjectById(this.selectedSubjectId);
    		if (subject){
    			this.examination.subjectId = this.selectedSubjectId;
    			this.examination.subjectName = subject.name;
    		}
    		this.examination.subjectId = this.selectedSubjectId;
    	}
    }
    
    loadExaminationAnesthetic(examination_id: number) {
        this.examAnestheticService.getExaminationAnesthetics(examination_id)
            .then(examAnesthetics => {
                if (examAnesthetics && examAnesthetics.length > 0) {
                    //Should be only one
                    let examAnesthetic: ExaminationAnesthetic = examAnesthetics[0];
                    //examAnesthetic.dose_unit = this.getReferenceById(examAnesthetic.dose_unit);
                    //examAnesthetic.anesthetic = this.getAnestheticById(examAnesthetic.anesthetic);    
                    this.examAnesthetic = examAnesthetic;
                }
            });

    }
    
    loadExtraDatas(){
        this.extradatasService.getExtraDatas(this.examination.id).then(extradatas => {
            if(extradatas){
                this.examinationExtradatas = extradatas;
            }else{
                this.examinationExtradatas = [];
            }
            this.loadExaminationExtraDatas();
        }).catch((error) => {
            this.examinationExtradatas = [];
        });
    }
    
    loadExaminationExtraDatas(){
    	for (let ex of this.examinationExtradatas) {
    		// instanceof does not work??
    		if (ex.extradatatype == "Physiological data"){
    			this.examinationPhysioData = <PhysiologicalData> ex;
    		}else {
    			this.examinationBloodGasData = <BloodGasData>ex;
    		}
    	}
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
        this.updateSubject();
        return super.save();
    }

    manageExaminationAnesthetic(examination_id : number) {
        if (this.examAnesthetic) {
            this.examAnesthetic.examination_id = examination_id;
            if (this.examAnesthetic.id) {
                this.examAnestheticService.updateAnesthetic(examination_id, this.examAnesthetic)
                    .subscribe(examAnesthetic => {
                    });
            } else if (this.examAnesthetic.anesthetic){
                this.examAnestheticService.createAnesthetic(examination_id, this.examAnesthetic)
                    .subscribe(examAnesthetic => {
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
            	this.extradatasService.update(PreclinicalUtils.PRECLINICAL_PHYSIO_DATA,this.examinationPhysioData.id, this.physioData)
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
            	this.extradatasService.create(PreclinicalUtils.PRECLINICAL_PHYSIO_DATA, this.physioData)
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
            	this.extradatasService.update(PreclinicalUtils.PRECLINICAL_BLOODGAS_DATA, this.examinationBloodGasData.id, this.bloodGasData)
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
            	this.extradatasService.create(PreclinicalUtils.PRECLINICAL_BLOODGAS_DATA, this.bloodGasData)
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