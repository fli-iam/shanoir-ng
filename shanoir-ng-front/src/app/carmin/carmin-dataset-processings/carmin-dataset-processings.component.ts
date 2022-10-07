import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { BreadcrumbsService } from 'src/app/breadcrumbs/breadcrumbs.service';
import { EntityListComponent } from 'src/app/shared/components/entity/entity-list.component.abstract';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { BrowserPaging } from 'src/app/shared/components/table/browser-paging.model';
import { Page, Pageable } from 'src/app/shared/components/table/pageable.model';
import { TableComponent } from 'src/app/shared/components/table/table.component';
import { CarminDatasetProcessing } from '../models/CarminDatasetProcessing';
import { CarminDatasetProcessingService } from '../shared/carmin-dataset-processing.service';

@Component({
  selector: 'app-carmin-dataset-processings',
  templateUrl: './carmin-dataset-processings.component.html',
  styleUrls: ['./carmin-dataset-processings.component.css']
})
export class CarminDatasetProcessingsComponent extends EntityListComponent<CarminDatasetProcessing> implements AfterViewInit {

  @ViewChild('table', { static: false }) table: TableComponent;
  private caminDatasetProcessings: CarminDatasetProcessing[] = [];

  constructor(protected breadcrumbsService: BreadcrumbsService, private carminDatasetProcessingService: CarminDatasetProcessingService) {
    super('carmin-dataset-processings');
    this.breadcrumbsService.markMilestone();
    this.breadcrumbsService.nameStep('VIP dataset processings');
  }

  ngAfterViewInit(): void {
    this.subscribtions.push(
      this.carminDatasetProcessingService.getAllCarminDatasetProcessings().subscribe(caminDatasetProcessings => {
        if (caminDatasetProcessings == null) {
          this.caminDatasetProcessings = [];
        } else {
          this.caminDatasetProcessings = caminDatasetProcessings;
        }
        this.table.refresh();
      })
    );
  }

  getService(): EntityService<CarminDatasetProcessing> {
    return this.carminDatasetProcessingService;
  }

  getOptions() {
    return { 'new': false, 'edit': false, 'view': false, 'delete': false, 'reload': true, id: false };
  }

  getPage(pageable: Pageable): Promise<Page<CarminDatasetProcessing>> {
    return Promise.resolve(new BrowserPaging(this.caminDatasetProcessings, this.columnDefs).getPage(pageable));
  }

  getColumnDefs(): any[] {
    function dateRenderer(date: number) {
      if (date) {
        return new Date(date).toLocaleString();
      }
      return null;
    };
    return [
      {headerName: "ID", field: "id", width: '130px', defaultSortCol: true, defaultAsc: false},
      {
        headerName: 'Name', field: 'name', width: '100%', type: 'link',
        route: (carminDatasetProcessing: CarminDatasetProcessing) => {
          // return the link of the carmin dataset processing + id
          return `/dataset-processing/details/${carminDatasetProcessing.id}`;
        }
      },
      { headerName: 'Status', field: 'status', width: '70px', type: 'Status' },
      {
        headerName: "Creation", field: "startDate", width: '130px', cellRenderer: function (params: any) {
          return dateRenderer(params.data.startDate);
        }
      },
      {
        headerName: "Workflow ID", field: "comment", width: '130px'
      },
    ];
  }

  getCustomActionsDefs(): any[] {
    return [];
  }

}
