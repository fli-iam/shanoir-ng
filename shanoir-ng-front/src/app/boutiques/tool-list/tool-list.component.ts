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
import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';

import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../../shared/components/table/table.component';
import { ToolInfo } from '../tool.model';
import { ToolService } from '../tool.service';

@Component({
  selector: 'tool-list',
  templateUrl: 'tool-list.component.html',
  styleUrls: ['tool-list.component.css'],
})
export class ToolListComponent extends BrowserPaginEntityListComponent<ToolInfo> {
  
  @Output() toolSelected = new EventEmitter<ToolInfo>();
  @ViewChild('table', { static: true }) table: TableComponent;

  constructor(private toolService: ToolService) {
    super('boutiques');
    this.breadcrumbsService.nameStep('Boutiques');
  }

  getOptions() {
    return {
      new: false,
      view: false, 
      edit: false, 
      delete: false
    };
  }

  getEntities(): Promise<ToolInfo[]> {
    return this.toolService.getAll();
  }

  getColumnDefs(): any[] {
    let colDef: any[] = [
      { headerName: "Name", field: "name" },
      { headerName: "Description", field: "description" },
      { headerName: "Id", field: "id" },
      { headerName: "Downloads", field: "nDownloads" }
    ];
    return colDef;
  }

  getCustomActionsDefs(): any[] {
    return [];
  }

  onToolSelected(toolInfo: ToolInfo): void {
    this.toolSelected.emit(toolInfo)
  }
}