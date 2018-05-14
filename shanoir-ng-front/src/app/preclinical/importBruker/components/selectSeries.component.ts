import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { slideDown, preventInitialChildAnimations } from '../../../shared/animations/animations';
import { ImportBrukerService } from '../importBruker.service';


import { StudyService } from '../../../studies/shared/study.service';
import { ImportJob, PatientDicom, SerieDicom, EquipmentDicom } from "../../../import/dicom-data.model";
import { Study } from '../../../studies/shared/study.model';
import { StudyCard } from '../../../study-cards/shared/study-card.model';

@Component({
    selector: 'select-series-bruker',
    templateUrl: 'selectSeries.component.html',
    styleUrls: ['../importBruker.component.css'],
    animations: [slideDown, preventInitialChildAnimations]
})

export class SelectSeriesComponent implements OnInit {
	@Input() enabled :boolean = true;
	@Input() tab_open :boolean = true;
	@Output() selectSeriesReady = new EventEmitter();
    public selectSeriesForm: FormGroup;
    
    
    public patients: PatientDicom[];
    private patientDicom: PatientDicom;
    private detailedPatient: Object;
    private detailedSerie: Object;
    public studies: Study[];
    public study: Study;
    public examinationComment: string;
    public seriesSelected: boolean = false;
    private selectedSeries: PatientDicom;
    public studycards: StudyCard[];
    public studycard: StudyCard;
    public studycardMissingError: Boolean;
    
    constructor(
    	private fb: FormBuilder,
    	private importBrukerService: ImportBrukerService,
        private studyService: StudyService,
    ) {}
    
    
	ngOnInit(): void {
        this.buildForm();
	}
	
	buildForm(): void {
        this.selectSeriesForm = this.fb.group({
        });
    
        this.selectSeriesForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }
        
    onValueChanged(data?: any): void {
        if (!this.selectSeriesForm) { return; }
        const form = this.selectSeriesForm;
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
    };
    
    
    showSerieDetails(nodeParams: any): void {
        if (nodeParams && this.detailedSerie && nodeParams.id == this.detailedSerie["id"]) {
            this.detailedSerie = null;
        } else {
            this.detailedSerie = nodeParams;
        }
    }

    showPatientDetails(nodeParams: any): void {
        if (nodeParams && this.detailedPatient && nodeParams.id == this.detailedPatient["id"]) {
            this.detailedPatient = null;
        } else {
            this.detailedPatient = nodeParams;
        }
    }
    
    selectNode(nodeParams: PatientDicom): void {
        this.selectedSeries = nodeParams;
        for (let study of nodeParams.studies) {
            for (let serie of study.series) {
                this.seriesSelected = false;
                if (serie.selected) {
                    this.seriesSelected = true;
                    break;
                } 
            }
        }
    }

    changeExamComment (editedLabel: string): void {
        this.examinationComment = editedLabel;
    }
    
    
    validateSeriesSelected () : void {
        this.findStudiesWithStudyCardsByUserAndEquipment(this.selectedSeries.studies[0].series[0].equipment);
        if(!this.examinationComment) {
            // initialize examComment with the studyDescription Dicom value if this Dicom value is not changed by user
            this.examinationComment = this.selectedSeries.studies[0].studyDescription;
        }
    }
    
    
    findStudiesWithStudyCardsByUserAndEquipment(equipment: EquipmentDicom): void {
        this.studyService
            .findStudiesWithStudyCardsByUserAndEquipment(equipment)
            .then(studies => {
                this.prepareStudyStudycard(studies);
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting study and study card list by user and equipment!");
            });
    }

    prepareStudyStudycard(studies: Study[]): void {
        this.studies = studies;
        let compatibleStudies: Study[] = [];
        for (let study of studies) {
            if (study.compatible) {
                compatibleStudies.push(study);
            }
        }
        if (compatibleStudies.length == 1) {
            // autoselect study
            this.study = compatibleStudies[0];
            this.studycards = this.study.studyCards;
        }
    }
    
    onSelectStudy(study: Study): void {
        if (study) {
            if (study.studyCards.length == 0) {
                this.studycardMissingError = true;
            } else {
                this.studycardMissingError = false;
                let compatibleStudycards: StudyCard[] = [];
                for (let studycard of study.studyCards) {
                    if (studycard.compatible) {
                        compatibleStudycards.push(studycard);
                    }
                }
                if (compatibleStudycards.length == 1) {
                    // autoselect studycard
                    this.studycard = compatibleStudycards[0];
                } 
                this.studycards = study.studyCards;
            }
        }
    }
    
    
    
}