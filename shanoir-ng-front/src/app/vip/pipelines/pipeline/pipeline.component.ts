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

import { Component, Input, OnInit, ChangeDetectionStrategy } from '@angular/core';

import { Pipeline } from '@app/vip/models/pipeline';
import { ImagesUrlUtil } from '@app/shared/utils/images-url.util';


@Component({
    selector: 'app-pipeline',
    templateUrl: './pipeline.component.html',
    styleUrls: ['./pipeline.component.css'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: []
})
export class PipelineComponent implements OnInit {

  @Input() pipeline: Pipeline;
  @Input() selected: boolean = false;
  readonly ImagesUrlUtil = ImagesUrlUtil;

  ngOnInit(): void {
      return;
  }

  /** The identifier is built as "<name>/<version>", fall back on it when VIP does not send both fields. */
  get displayName(): string {
      return this.pipeline.name ?? this.pipeline.identifier?.split('/')[0] ?? this.pipeline.identifier;
  }

  get displayVersion(): string {
      return this.pipeline.version ?? this.pipeline.identifier?.split('/')[1];
  }

}
