import {Component, Input, ViewChild} from '@angular/core';
import {ColumnDefinition} from "../../shared/components/table/column.definition.type";
import {ShanoirEvent} from "../../users/shanoir-event/shanoir-event.model";
import {ShanoirEventService} from "../../users/shanoir-event/shanoir-event.service";
import {Study} from "../shared/study.model";
import {Page, Pageable} from "../../shared/components/table/pageable.model";
import {TableComponent} from "../../shared/components/table/table.component";
import {StudyUser} from "../shared/study-user.model";
import {Examination} from "../../examinations/shared/examination.model";

@Component({
  selector: 'study-history',
  templateUrl: './study-history.component.html',
  styleUrl: './study-history.component.css'
})
export class StudyHistoryComponent {

    @ViewChild('table', {static: false}) table: TableComponent;
    @Input() study: Study;
    @Input() eventHistory: Promise<any>;
    users: Map<number, string> = new Map();

    historyColumns: ColumnDefinition[] = [
        {headerName: 'Creation date', field: 'creationDate', type: 'dateTime'},
        {headerName: 'User', field: 'username'},
        {headerName: 'Event type', field: 'eventType', cellRenderer: function (params: any) {
                if (params.data.eventType.includes(".event")) {
                    params.data.eventType = params.data.eventType.replace(".event", "");
                }
                return params.data.eventType;
            }
        },
        {headerName: 'ObjectId', field: 'objectId', route: function(params:ShanoirEvent) {
                let event = params.eventType;
                let id = params.objectId;
                if (event.includes("create")) {
                    if (event.includes("Dataset") && !event.includes("Acquisition")) { return "/dataset/details/" + id; }
                    else if (event.includes("Examination")) { return "/examination/details/" + id; }
                    else if (event.includes("Acquisition")) { return "/dataset-acquisition/details/" + id; }
                    else if (event.includes("Subject")) { return "/subject/details/" + id; }
                } else if (event.includes("update")) {
                    if (event.includes("Study")) { return "/study/details/" + id; }
                    else if (event.includes("Examination")) { return "/examination/details/" + id; }
                    else if (event.includes("Subject")) { return "/subject/details/" + id; }
                } else if (event.includes("import")) {
                    if (event.includes("Dataset")) { return "/examination/details/" + id; }
                } else {
                    if (event.includes("userAddToStudy")) {
                        params.objectId = null;
                    }
                }
            }},
        {headerName: 'Message', field: 'message'}
    ];

    constructor(
        private shanoirEventService: ShanoirEventService
    ) {}
    ngOnInit() {
        this.eventHistory.then( () => this.getPage);
    }

    getPage(pageable: Pageable): Promise<Page<ShanoirEvent> | void> {
        return this.shanoirEventService.getPage(pageable, this.study.id, this.table.filter.searchStr? this.table.filter.searchStr : "", this.table.filter.searchField ? this.table.filter.searchField : "").then(page => {
            page.content.forEach(item => {
                if (this.users.get(item.userId) == undefined) {
                    let studyUser : StudyUser;
                    studyUser = this.study.studyUserList.find(user => user.userId == item.userId);
                    if (studyUser) {
                        this.users.set(item.userId, studyUser.userName);
                        item.username = studyUser.userName;
                    }
                } else {
                    item.username = this.users.get(item.userId).valueOf();
                }
            });
            return page;
        }).catch(reason => {
            if(reason?.error?.code != 403) {
                throw Error(reason);
            }
        });
    }
}
