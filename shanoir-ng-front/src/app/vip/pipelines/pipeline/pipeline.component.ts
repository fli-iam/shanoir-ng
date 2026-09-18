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

import { Component, EventEmitter, Input, OnInit, Output, ChangeDetectionStrategy } from '@angular/core';

import { Pipeline } from '@app/vip/models/pipeline';
import { PipelineGroup, PipelineVersion } from '@app/vip/models/pipeline-group';
import { ImagesUrlUtil } from '@app/shared/utils/images-url.util';


@Component({
    selector: 'app-pipeline',
    templateUrl: './pipeline.component.html',
    styleUrls: ['./pipeline.component.css'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: []
})
export class PipelineComponent implements OnInit {

  @Input() group: PipelineGroup;
  /** Identifier of the pipeline currently selected in the whole list, may belong to another tile. */
  @Input() selectedIdentifier: string;
  @Output() pipelineSelected: EventEmitter<Pipeline> = new EventEmitter<Pipeline>();

  /** The version the picker is on, latest by default. */
  pickedVersion: PipelineVersion;
  readonly ImagesUrlUtil = ImagesUrlUtil;

  ngOnInit(): void {
      this.pickedVersion = this.group?.versions[0];
  }

  get selected(): boolean {
      return !!this.pickedVersion && this.pickedVersion.pipeline.identifier === this.selectedIdentifier;
  }

  select(): void {
      if (this.pickedVersion) {
          this.pipelineSelected.emit(this.pickedVersion.pipeline);
      }
  }

  pickVersion(label: string): void {
      this.pickedVersion = this.group.versions.find(version => version.label === label) ?? this.pickedVersion;
      this.select();
  }

}
