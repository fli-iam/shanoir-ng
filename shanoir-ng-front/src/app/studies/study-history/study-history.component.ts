import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ColumnDefinition} from "../../shared/components/table/column.definition.type";
import {ShanoirEvent} from "../../users/shanoir-event/shanoir-event.model";
import {ShanoirEventService} from "../../users/shanoir-event/shanoir-event.service";
import {Study} from "../shared/study.model";
import {Page, Pageable} from "../../shared/components/table/pageable.model";
import {BrowserPaging} from "../../shared/components/table/browser-paging.model";
import {TableComponent} from "../../shared/components/table/table.component";

@Component({
  selector: 'study-history',
  templateUrl: './study-history.component.html',
  styleUrl: './study-history.component.css'
})
export class StudyHistoryComponent {

    @Input() study: Study;

    history: ShanoirEvent[]=[];
    table: TableComponent;

    historyColumns: ColumnDefinition[] = [
        {headerName: 'Creation date', field: 'creationDate', type: 'dateTime'},
        {headerName: 'User', field: 'userId'},
        {headerName: 'Event type', field: 'eventType', cellRenderer: function (params: any) {
                if (params.data.eventType.includes(".event")) {
                    params.data.eventType = params.data.eventType.replace(".event", "");
                }
                return params.data.eventType;
            }
        },
        {headerName: 'ObjectId', field: 'objectId'},
        {headerName: 'Message', field: 'message'}
    ];

    constructor(
        private shanoirEventService: ShanoirEventService
    ) {
        console.log("study history constructor");
    }

    ngOnInit() {
        console.log("ngOnInit study-history: study ", this.study);
    }

    public onFetchHistory(e: any) {
        console.log("onFetchHistory ", e);
        this.shanoirEventService.requestHistory(this.study.id).then(history => {
            this.history = history;
        });
    }

    getPage = (pageable: Pageable): Promise<Page<ShanoirEvent>> => {
        return Promise.resolve(new BrowserPaging(this.history, this.historyColumns).getPage(pageable));
    }

}
