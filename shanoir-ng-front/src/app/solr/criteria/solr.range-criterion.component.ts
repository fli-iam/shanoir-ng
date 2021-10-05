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
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { slideDown } from '../../shared/animations/animations';
import { Range } from '../../shared/models/range.model';


@Component({
    selector: 'solr-range-criterion',
    templateUrl: 'solr.range-criterion.component.html',
    styleUrls: ['solr.criterion.component.css', 'solr.range-criterion.component.css'], 
    animations: [slideDown]
})

export class SolrRangeCriterionComponent {
    
    @Input() range: Range = new Range(null, null);
    @Input() label: string = "";
    @Output() onChange: EventEmitter<Range> = new EventEmitter();

    private tiemout: number;
    private changing: boolean = false;
    open: boolean = false;

    change() {
        const delay: number = 1;
        if (this.changing) {
            this.tiemout = delay;
        } else {
            this.changing = true;
            this.tiemout = delay;
            let interval = setInterval(() => {
                this.tiemout = this.tiemout - 0.4;
                if (this.tiemout < 0) {
                    clearInterval(interval);
                    this.changing = false;
                    this.onChange.emit(this.range);
                }
            }, 400);
        }

    }

}