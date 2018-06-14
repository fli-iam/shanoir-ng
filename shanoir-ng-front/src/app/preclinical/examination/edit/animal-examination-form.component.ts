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

import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { ImagesUrlUtil } from "../../../shared/utils/images-url.util";

import { KeycloakService } from "../../../shared/keycloak/keycloak.service";

import { IMyDate, IMyDateModel, IMyInputFieldChanged, IMyOptions } from 'mydatepicker';

import { Mode } from "../../shared/mode/mode.model";
import { Modes } from "../../shared/mode/mode.enum";
import { ModesAware } from "../../shared/mode/mode.decorator";

@Component({
    selector: 'examination-preclinical-form',
    templateUrl: 'animal-examination-form.component.html',
    providers: [ExaminationExtraDataService, ContrastAgentService, ExaminationAnestheticService, AnimalExaminationService],
    styleUrls: ['animal-examination.component.css']
})
@ModesAware
export class AnimalExaminationFormComponent implements OnInit {

	@ViewChild('instAssessmentModal') instAssessmentModal: ModalComponent;
    @ViewChild('attachNewFilesModal') attachNewFilesModal: ModalComponent;
    
    @Input() mode: Mode = new Mode();
    @Input() preFillData: Examination;
    public examination_id: number;
    public examination: Examination = new Examination();
    //TO BE RETRIEVED THROUGH DATASET EXAMINATION
    public protocol_id: number;
    @Output() closing = new EventEmitter();
    newExamForm: FormGroup;
    private canModify: Boolean = false;
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
    private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;
    isDateValid: boolean = true;
    selectedDateNormal: IMyDate;
    

    constructor(
        private examAnestheticService: ExaminationAnestheticService,
        private extradatasService: ExaminationExtraDataService,
        private contrastAgentsService: ContrastAgentService,
        private centerService: CenterService,
        private studyService: StudyService,
        private animalExaminationService: AnimalExaminationService,
        private keycloakService: KeycloakService,
        public router: Router,
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private location: Location) {

    }
    
    
    ngOnInit(): void {
    	this.getCenters();
        this.getStudies();
        this.getExamination();
        this.buildForm();
        this.initPrefillData();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }
    
    ngOnChanges(changes: SimpleChanges) {
        if (changes['preFillData']) this.initPrefillData();
    }

    
    
    getCenters(): void {
    	this.centers = [];
        this.centerService
            .getCentersNamesForExamination()
            .then(centers => {
                this.centers = centers;
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting center list!");
            });
    }

    getStudies(): void {
    	this.studies = [];
        this.studyService
            .getStudiesNames()
            .then(studies => {
                this.studies = studies;
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting study list!");
            });
    }


	getExamination(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let examId = queryParams['id'];
                let mode = queryParams['mode'];
                if (mode) {
                    this.mode.setModeFromParameter(mode);
                }
                if (examId) {
                    // view or edit mode
                    this.examination_id = examId;
                    this.loadExaminationAnesthetic();
                    this.loadExtraDatas();
                    return this.animalExaminationService.getExamination(examId);
                } else {
                    // create mode
                    return Observable.of<Examination>();
                }
            })
            .subscribe(examination => {
                if (!this.mode.isCreateMode()) {
					this.examination = examination;
                	this.getDateToDatePicker(this.examination);
                }
                
            });
    }
    
    
    
    loadExtraDatas(){
        this.extradatasService.getExtraDatas(this.examination_id).then(extradatas => {
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
    
      
    loadExaminationAnesthetic() {
        this.examAnestheticService.getExaminationAnesthetics(this.examination_id)
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
    

    buildForm(): void {
        this.newExamForm = this.fb.group({
        	'id': [this.examination.id],
            'studyId': [this.examination.studyId, Validators.required],
            // 'Examination executive': [this.examination.examinationExecutive],
            'centerId': [this.examination.centerId, Validators.required],
            // 'Subject': [this.examination.subject],
            'examinationDate': [this.examination.examinationDate],
            'comment': [this.examination.comment],
            'note': [this.examination.note],
            'subjectWeight': [this.examination.subjectWeight],
            //regarding examination anesthetic
            newExamAnestheticForm: this.fb.group({
                'anesthetic': [this.examAnesthetic.anesthetic],
                'injectionInterval': [this.examAnesthetic.injection_interval],
                'injectionSite': [this.examAnesthetic.injection_site],
                'injectionType': [this.examAnesthetic.injection_type],
                'dose': [this.examAnesthetic.dose],
                'dose_unit': [this.examAnesthetic.dose_unit]
               // 'startDate': [this.examAnesthetic.startDate],
               // 'endDate': [this.examAnesthetic.endDate]
            }),
            //regarding contrast agent
            //newAgentForm : this.fb.group({
            //    'name': [this.contrastAgent.name,Validators.required],
            //    'manufactured_name': [this.contrastAgent.manufactured_name],
             //   'dose': [this.contrastAgent.dose],
             //   'dose_unit': [this.contrastAgent.dose_unit],
              //  'concentration': [this.contrastAgent.concentration],
             //   'concentration_unit': [this.contrastAgent.concentration_unit],
             //   'injectionInterval': [this.contrastAgent.injection_interval],
             //   'injectionSite': [this.contrastAgent.injection_site],
             //   'injectionType': [this.contrastAgent.injection_type]
           // })
        });

        this.newExamForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
    }

    onValueChanged(data?: any) {
        if (!this.newExamForm) { return; }
        const form = this.newExamForm;
        for (const field in this.formErrors) {
            // clear previous error message (if any)
            this.formErrors[field] = '';
            const control = form.get(field);
            if (control && control.dirty && !control.valid) {
                for (const key in control.errors) {
                    this.formErrors[field] += key;
                }
            }
        }
    }

    formErrors = {
        'centerId': '',
        'studyId': ''
    };

    back(examination?: Examination): void {
        if (this.closing.observers.length > 0) {
        	if (examination){
            	this.closing.emit(examination);
            	this.examination = new Examination();
            }else{
            	this.closing.emit(new Examination());
            }
        } else {
            this.location.back();
        }
    }

    edit(): void {
        this.router.navigate(['/preclinical-examination'], { queryParams: { id: this.examination_id, mode: "edit" } });
    }
    
    
    
    create() {
        this.setDateFromDatePicker();
        this.examination.preclinical = true;
        this.animalExaminationService.create(this.examination)
            .subscribe((examination) => {
            	this.examination_id = examination.id;
                //Following functions should be called after examination creation
        		this.manageExaminationAnesthetic();
        		//this.manageContrastAgent();
        		this.addExtraDataToExamination(this.examination_id, false);

        		this.back(examination);
            }, (err: String) => {

       });
    }


    update() {
    	this.setDateFromDatePicker();
        this.examination.preclinical = true;
        this.animalExaminationService.update(this.examination_id, this.examination)
            .subscribe((examination) => {
        		this.manageExaminationAnesthetic();
       			//this.manageContrastAgent();
        		this.addExtraDataToExamination(this.examination_id, true);

        		this.back();
            }, (err: String) => {

       });
    }

    manageExaminationAnesthetic() {
        if (this.examAnesthetic) {
            this.examAnesthetic.examination_id = this.examination_id;
            if (this.examAnesthetic.id) {
                this.examAnestheticService.update(this.examination_id, this.examAnesthetic)
                    .subscribe(examAnesthetic => {
                    });
            } else if (this.examAnesthetic.anesthetic){
                this.examAnestheticService.create(this.examination_id, this.examAnesthetic)
                    .subscribe(examAnesthetic => {
                    });
            }
        }
    }

    manageContrastAgent() {
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
    }

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
    
    setDateFromDatePicker(): void {
        if (this.selectedDateNormal) {
            this.examination.examinationDate = new Date(this.selectedDateNormal.year, this.selectedDateNormal.month - 1,
                this.selectedDateNormal.day);
        } else {
            this.examination.examinationDate = null;
        }
    }

    getDateToDatePicker(examination: Examination): void {
        if (examination && examination.examinationDate && !isNaN(new Date(examination.examinationDate).getTime())) {
            let expirationDate: Date = new Date(examination.examinationDate);
            this.selectedDateNormal = {
                year: expirationDate.getFullYear(), month: expirationDate.getMonth() + 1,
                day: expirationDate.getDate()
            };;
        }
    }
    
    onInputFieldChanged(event: IMyInputFieldChanged) {
        if (event.value !== '') {
            if (!event.valid) {
                this.isDateValid = false;
            } else {
                this.isDateValid = true;
            }
        } else {
            this.isDateValid = true;
            setTimeout(():void => this.selectedDateNormal = null);
        }
    }

    private myDatePickerOptions: IMyOptions = {
        dateFormat: 'dd/mm/yyyy',
        height: '20px',
        width: '160px'
    };

    onDateChanged(event: IMyDateModel) {
        if (event.formatted !== '') {
            this.selectedDateNormal = event.date;
        }
    }
    
    initPrefillData() {
        if (this.preFillData && this.examination) {
            this.examination.studyName = this.preFillData.studyName;
            this.examination.studyId = this.preFillData.studyId;
            this.examination.centerId = this.preFillData.centerId;
            this.examination.centerName = this.preFillData.centerName;
            this.examination.examinationDate = new Date(this.preFillData.examinationDate);
            this.examination.comment = this.preFillData.comment;
        }
    }
    


}