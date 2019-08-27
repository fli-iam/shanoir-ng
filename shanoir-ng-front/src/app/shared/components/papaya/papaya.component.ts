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

import { Component, OnInit, SimpleChange, Input } from "@angular/core";
import { ImagesUrlUtil } from "../../utils/images-url.util";

declare var papaya: any;
declare var papayaContainers: any[];

@Component({
    selector: "papaya",
    templateUrl: "papaya.component.html",
    styleUrls: ["papaya.component.css"]
})
export class PapayaComponent implements OnInit {
    @Input() params: any[];
    @Input() autoLoading: boolean = false;
    private loaded: boolean = false;
    private static loading: boolean = false;

    constructor() {}

    ngOnInit() {
        papayaContainers = [];
        papaya.Container.startPapaya();
    }

    ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
        for (let propName in changes) {
            if (!changes[propName].isFirstChange()) {
                if (propName == "params") {
                    this.setDefaultParams();
                    if (this.autoLoading) {
                        this.load();
                    } else {
                        this.loaded = false;
                    }
                }
            }
        }
    }

    private load() {
        if (!PapayaComponent.loading) {
            this.loaded = true; 
            PapayaComponent.loading = true;
            this.params["loadingComplete"] = () => { PapayaComponent.loading = false; };
            papaya.Container.resetViewer(0, this.params);
        } else {
            throw new Error("Don't try to load an image in papaya before the previous loading is finished");
        }
    }

    public isLoading(): boolean {
        return PapayaComponent.loading;
    }

    private setDefaultParams() {
        if (!this.params["allowScroll"]) this.params["allowScroll"] = false;
    }

}