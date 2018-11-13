import {Component, ViewChild} from '@angular/core'

import { Therapy } from '../shared/therapy.model';
import { TherapyService } from '../shared/therapy.service';
import { TherapyType } from "../../../shared/enum/therapyType";
import { SubjectTherapyService } from '../../subjectTherapy/shared/subjectTherapy.service';
import { TableComponent } from '../../../../shared/components/table/table.component';
import { BrowserPaginEntityListComponent } from '../../../../shared/components/entity/entity-list.browser.component.abstract';
import { ServiceLocator } from '../../../../utils/locator.service';
import { ShanoirError } from '../../../../shared/models/error.model';


@Component({
  selector: 'therapy-list',
  templateUrl:'therapy-list.component.html',
  styleUrls: ['therapy-list.component.css'], 
  providers: [TherapyService]
})
export class TherapiesListComponent  extends BrowserPaginEntityListComponent<Therapy>{
  @ViewChild('therapiesTable') table: TableComponent;
    
    constructor(
        private therapyService: TherapyService, 
        private subjectTherapyService: SubjectTherapyService) {
            super('preclinical-therapy');
        }

    getEntities(): Promise<Therapy[]> {
        return this.therapyService.getAll();
    }

    getColumnDefs(): any[] {
        let colDef: any[] = [
            {headerName: "Name", field: "name"},
            {headerName: "Type", field: "therapyType", type: "Enum", cellRenderer: function (params: any) {
                return TherapyType[params.data.therapyType];
            }},
            {headerName: "Comment", field: "comment"}   
        ];
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }


    protected openDeleteConfirmDialog = (entity: Therapy) => {
        this.subjectTherapyService.getAllSubjectForTherapy(entity.id).then(subjectTherapies => {
    		if (subjectTherapies){
    			let hasSubjects: boolean  = false;
    			hasSubjects = subjectTherapies.length > 0;
    			if (hasSubjects){
    				this.confirmDialogService
                		.confirm('Delete therapy', 'This therapy is linked to subjects, it can not be deleted', 
                        ServiceLocator.rootViewContainerRef)
    			}else{
    				this.openDeleteTherapyConfirmDialog(entity);
    			}
    		}else{
    			this.openDeleteTherapyConfirmDialog(entity);
    		}
    	}).catch((error) => {
    		console.log(error);
    		this.openDeleteTherapyConfirmDialog(entity);
    	});    
    }   

    private openDeleteTherapyConfirmDialog = (entity: Therapy) => {
        if (this.keycloakService.isUserGuest()) return;
        this.confirmDialogService
            .confirm(
                'Delete', 'Are you sure you want to delete preclinical-therapy n° ' + entity.id + ' ?',
                ServiceLocator.rootViewContainerRef
            ).subscribe(res => {
                if (res) {
                    entity.delete().then(() => {
                        this.onDelete.next(entity);
                        this.table.refresh();
                        this.msgBoxService.log('info', 'The preclinical-therapy sucessfully deleted');
                    }).catch(reason => {
                        if (reason && reason.error) {
                            this.onDelete.next(new ShanoirError(reason));
                            if (reason.error.code != 422) throw Error(reason);
                        }
                    });                    
                }
            })
    }

    
    
}