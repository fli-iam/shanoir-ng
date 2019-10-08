import {Component, ViewChild, ViewContainerRef} from '@angular/core'
import { Anesthetic } from '../shared/anesthetic.model';
import { AnestheticService } from '../shared/anesthetic.service';
import { AnestheticType } from "../../../shared/enum/anestheticType";
import { ExaminationAnestheticService } from '../../examination_anesthetic/shared/examinationAnesthetic.service';
import { TableComponent } from '../../../../shared/components/table/table.component';
import { BrowserPaginEntityListComponent } from '../../../../shared/components/entity/entity-list.browser.component.abstract';
import { ServiceLocator } from '../../../../utils/locator.service';
import { ShanoirError } from '../../../../shared/models/error.model';
import { MsgBoxService } from '../../../../shared/msg-box/msg-box.service';

@Component({
  selector: 'anesthetic-list',
  templateUrl:'anesthetic-list.component.html',
  styleUrls: ['anesthetic-list.component.css'], 
  providers: [AnestheticService]
})
export class AnestheticsListComponent  extends BrowserPaginEntityListComponent<Anesthetic>{
    
    @ViewChild('anestheticsTable') table: TableComponent;
    
    constructor(
        private anestheticsService: AnestheticService, 
        private examinationAnestheticService: ExaminationAnestheticService
    ) 
    {
        super('preclinical-anesthetic');
    }
    
    getEntities(): Promise<Anesthetic[]> {
        return this.anestheticsService.getAll();
    }
    
    getColumnDefs(): any[] {
        function checkNullValue(value: any) {
            if(value){
                return value;
            }
            return '';
        };
        let colDef: any[] = [
            {headerName: "Name", field: "name", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.name);
            }},
            {headerName: "Type", field: "anestheticType", type: "Enum", cellRenderer: function (params: any) {
                return AnestheticType[params.data.anestheticType];
            }},
            {headerName: "Comment", field: "comment", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.comment);
            }}    
        ];
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
    
    
    protected openDeleteConfirmDialog = (entity: Anesthetic) => {
        this.examinationAnestheticService.getAllExaminationForAnesthetic(entity.id).then(examinationAnesthetics => {
    		if (examinationAnesthetics){
    			let hasExams: boolean  = false;
    			hasExams = examinationAnesthetics.length > 0;
    			if (hasExams){
                    this.confirmDialogService
                        .confirm('Delete anesthetic', 'This anesthetic is linked to preclinical examinations, it can not be deleted', 
                    		ServiceLocator.rootViewContainerRef);
    			}else{
    				this.openDeleteAnestheticConfirmDialog(entity);
    			}
    		}else{
    			this.openDeleteAnestheticConfirmDialog(entity);
    		}
    	}).catch((error) => {
    		console.log(error);
    		this.openDeleteAnestheticConfirmDialog(entity);
    	});    
    }   

    private openDeleteAnestheticConfirmDialog = (entity: Anesthetic) => {
        if (!this.keycloakService.isUserAdminOrExpert()) return;
        this.confirmDialogService
            .confirm(
                'Delete', 'Are you sure you want to delete preclinical-anesthetic n° ' + entity.id + ' ?',
                ServiceLocator.rootViewContainerRef
            ).subscribe(res => {
                if (res) {
                    entity.delete().then(() => {
                        this.onDelete.next(entity);
                        this.table.refresh();
                        this.msgBoxService.log('info', 'The preclinical-anesthetic sucessfully deleted');
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