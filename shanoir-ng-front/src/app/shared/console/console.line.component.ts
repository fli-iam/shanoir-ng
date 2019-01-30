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

import { Component, Input } from '@angular/core';


@Component({
    selector: 'console-line',
    template: `
        <div [class.ok]="status=='ok'" [class.error]="status=='error'"><span>{{label}}</span><span [innerHTML]="getProgressBar()"></span></div>
    `,
    styles: [
        ':host() { }',
        '/*.ok { color: lawngreen; }*/',
        '.error { color: orangered; }'
    ]
})

export class ConsoleComponent {

    @Input() label: string;
    @Input() status: "ok" | "error";
    @Input() progress: number = 0;
    private width: number = 70;
    private symbol: string = ".";
    private okSymbol: string = "[DONE]";
    private errSymbol: string = "[ERROR]"

    private getMax(extraSymbol: string): number {
        return (this.width - this.label.length - extraSymbol.length);
    }

    private getCurrentNbSymbols(extraSymbol: string): number {
        return this.getMax(extraSymbol) * this.getProgress();
    }

    getProgressBar(): string {
        let content: string = "";
        let i;
        for (i=0; i<this.getCurrentNbSymbols(this.okSymbol); i++) {
            content += this.symbol;
        } 
        if (this.status == 'ok') {
            content += this.okSymbol;
        } else if (this.status == 'error') {
            /*for (i; i<this.getMax(this.errSymbol); i++) {
                content += "&nbsp;";
            }*/
            content += this.errSymbol;
        } else {
            //content = content.slice(0, content.length-1-5) + "["+this.progress*100+"%]";
        }
        return content; 
    }

    getProgress(): number {
        return this.progress < 1 ? this.progress : 1;
    }
} 