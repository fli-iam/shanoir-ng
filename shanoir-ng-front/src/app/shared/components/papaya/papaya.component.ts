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

import { Component, OnInit, SimpleChanges, Input, HostBinding, OnDestroy, OnChanges } from "@angular/core";


declare let papaya: any;
declare let papayaContainers: any[];

@Component({
    selector: "papaya",
    templateUrl: "papaya.component.html",
    styleUrls: ["papaya.component.css"],
    imports: []
})
export class PapayaComponent implements OnInit, OnDestroy, OnChanges {
    @Input() params: any[];
    @Input() autoLoading: boolean = false;
    @Input() loadingCallback: () => Promise<any[]>;
    @HostBinding('class.expanded') downloaded: boolean = false;
    private loading: boolean = false;
    protected error: boolean = false;

    ngOnInit() {
        papayaContainers = [];
        papaya.Container.startPapaya();
    }

    ngOnChanges(changes: SimpleChanges ) {
        if ((changes.params?.currentValue)
                || (changes.loadingCallback?.currentValue)) {
            this.setDefaultParams();
            if (this.autoLoading) {
                queueMicrotask(() => {
                    this.load();
                });
            } else {
                this.downloaded = false;
            }
        } 
    }

    ngOnDestroy(): void {
        papayaContainers[0].collapseViewer();
    }

    protected load() {
        if (this.loadingCallback) {
            this.loadIntoPapaya(this.loadingCallback);
        } else {
            this.loadIntoPapaya(() => Promise.resolve(this.params));
        }
    }

    private loadIntoPapaya(loadingCallback: () => Promise<any[]>) {
        if (!this.loading) {
            this.loading = true;
            loadingCallback()?.then(params => {
                this.downloaded = true; 
                params["loadingComplete"] = () => { this.loading = false; };
                papaya.Container.resetViewer(0, params);
            }).catch(reason => {
                this.error = true;
                console.error(reason);
            });
        } else {
            throw new Error("Don't try to load an image in papaya before the previous loading is finished");
        }
    }

    public isLoading(): boolean {
        return this.loading;
    }

    private setDefaultParams() {
        if (!this.params) this.params = [];
        this.params["allowScroll"] = false;
        this.params["ignoreNiftiTransforms"] =  true;
        this.params['expandable'] = true;
    }

}