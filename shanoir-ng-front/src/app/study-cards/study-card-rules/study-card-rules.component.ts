/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */
import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { StudyCardRule } from '../shared/study-card.model';
import { Mode } from '../../shared/components/entity/entity.component.abstract';
import { AssignmentField } from './action/action.component';
import { Option } from '../../shared/select/select.component';
import { ExploredEntity } from '../../enum/explored-entity.enum';
import { AcquisitionContrast } from '../../enum/acquisition-contrast.enum';
import { MrSequenceApplication } from '../../enum/mr-sequence-application.enum';
import { CoilService } from '../../coils/shared/coil.service';
import { Coil } from '../../coils/shared/coil.model';
import { MrSequencePhysics } from '../../enum/mr-sequence-physics.enum';


@Component({
    selector: 'study-card-rules',
    templateUrl: 'study-card-rules.component.html',
    styleUrls: ['study-card-rules.component.css']
})
export class StudyCardRulesComponent implements OnChanges {
    
    @Input() mode: Mode;
    @Input() rules: StudyCardRule[];
    @Output() rulesChange: EventEmitter<StudyCardRule> = new EventEmitter();
    @Input() manufModelId: number;
    fields: AssignmentField[];
    private coilOptions: Option<Coil>[] = [];
    private allCoils: Coil[];
    private allCoilsPromise: Promise<any>;
    
    constructor(private coilService: CoilService) {
        
        this.allCoilsPromise = this.coilService.getAll().then(coils => this.allCoils = coils);
        
        this.fields = [
            new AssignmentField('Dataset modality type', 'datasetMetadata.modalityType', [
                new Option<string>('Mr', 'Mr'), 
                new Option<string>('Pet', 'Pet')
            ]),
            new AssignmentField('Protocol name', 'mrProtocolMetadata.name'),
            new AssignmentField('Protocol comment', 'mrProtocolMetadata.comment'),
            new AssignmentField('Transmitting coil', 'mrProtocolMetadata.transmittingCoilId', this.coilOptions),
            new AssignmentField('Receiving coil', 'mrProtocolMetadata.receivingCoilId', this.coilOptions),
            new AssignmentField('Explored entity', 'datasetMetadata.exploredEntity', ExploredEntity.toOptions()),
            new AssignmentField('Acquisition contrast', 'mrProtocolMetadata.acquisitionContrast', AcquisitionContrast.toOptions()),
            new AssignmentField('MR sequence application', 'mrProtocolMetadata.mrSequenceApplication', MrSequenceApplication.toOptions()),
            new AssignmentField('MR sequence physics', 'mrProtocolMetadata.mrSequencePhysics', MrSequencePhysics.toOptions()),
            // new AssignmentField('', 'mrProtocolMetadata.'),
            // new AssignmentField('', 'mrProtocolMetadata.'),
            // new AssignmentField('', 'mrProtocolMetadata.'),
            // new AssignmentField('', 'mrProtocolMetadata.'),
            // new AssignmentField('', 'mrProtocolMetadata.'),
            // new AssignmentField('', 'mrProtocolMetadata.'),
            // new AssignmentField('', 'mrProtocolMetadata.'),
            // new AssignmentField('', 'mrProtocolMetadata.'),
            // new AssignmentField('', 'mrProtocolMetadata.'),
            // new AssignmentField('', 'mrProtocolMetadata.'),
        ];
    }
    
    ngOnChanges(changes: SimpleChanges): void {
        if (changes.manufModelId) {
            if (this.manufModelId) {
                this.allCoilsPromise.then(() => {
                    this.coilOptions.length = 0;
                    this.allCoils
                        .filter(coil => coil.manufacturerModel.id == this.manufModelId)
                        .forEach(coil => this.coilOptions.push(new Option<Coil>(coil, coil.name)));
                });
            } else {
                this.coilOptions.length = 0;
            }
        }
    }
    
}