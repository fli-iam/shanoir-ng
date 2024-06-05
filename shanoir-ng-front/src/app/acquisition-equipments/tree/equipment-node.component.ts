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
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';

import { AcquisitionEquipmentNode } from '../../tree/tree.model';
import { AcquisitionEquipment } from '../shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from "../shared/acquisition-equipment.service";
import { Selection, TreeService } from 'src/app/studies/study/tree.service';


@Component({
    selector: 'equipment-node',
    templateUrl: 'equipment-node.component.html'
})

export class EquipmentNodeComponent implements OnChanges {

    @Input() input: AcquisitionEquipmentNode | AcquisitionEquipment;
    @Output() selectedChange: EventEmitter<void> = new EventEmitter();
    @Output() onEquipmentDelete: EventEmitter<void> = new EventEmitter();
    @Input() withMenu: boolean = true;

    node: AcquisitionEquipmentNode;
    loading: boolean = false;
    menuOpened: boolean = false;
    detailsPath: string = '/acquisition-equipment/details/';

    constructor(
        private equipmentService: AcquisitionEquipmentService,
        protected treeService: TreeService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof AcquisitionEquipmentNode) {
                this.node = this.input;
            } else {
                throw new Error('not implemented yet');
            }
        }
    }

    deleteEquipment() {
        this.equipmentService.get(this.node.id).then(entity => {
            this.equipmentService.deleteWithConfirmDialog(this.node.title, entity).then(deleted => {
                if (deleted) {
                    this.onEquipmentDelete.emit();
                }
            });
        })
    }
}
