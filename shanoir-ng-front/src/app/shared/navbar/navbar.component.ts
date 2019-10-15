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

import { Component, ViewChildren, QueryList } from '@angular/core';

import { DropdownMenuComponent } from '../components/dropdown-menu/dropdown-menu.component'
import { KeycloakService } from '../keycloak/keycloak.service';
import { ImagesUrlUtil } from '../utils/images-url.util';
import { process } from '../../process';

@Component({
    selector: 'navbar',
    templateUrl: 'navbar.component.html',
    styleUrls: ['navbar.component.css']
})

export class NavbarComponent {

    @ViewChildren(DropdownMenuComponent) dropdownMenus: QueryList<DropdownMenuComponent>;

    public dev: boolean = (process.env.ENV == "development");
    private colorASave: string;
    private colorBSave: string;
    public mode: "default" | "pink" | "xtremPink" = "default";

    /* Icons */
    public ImagesUrlUtil = ImagesUrlUtil;

    constructor(private keycloakService: KeycloakService) {
    }

    ngAfterViewInit() {
        this.dropdownMenus.forEach((dropdownMenu) => {
            dropdownMenu.parent = this;
        });

        this.colorASave = document.documentElement.style.getPropertyValue("--color-a");
        this.colorBSave = document.documentElement.style.getPropertyValue("--color-b");
    }

    isUserAdmin(): boolean {
        return this.keycloakService.isUserAdmin();
    }
    
    canUserImportFromPACS(): boolean {
        return this.keycloakService.canUserImportFromPACS();
    }

    togglePink() {
        this.mode = this.mode == "pink" ? "default" : "pink";
        this.resetColors();
        document.documentElement.style.setProperty("--color-a", this.mode == "pink" ? "mediumvioletred" : this.colorASave);
        document.documentElement.style.setProperty("--color-b", this.mode == "pink" ? "hotpink" : this.colorBSave);
    }

    toggleXtremPink() {
        this.mode = this.mode == "xtremPink" ? "default" : "xtremPink";
        this.resetColors();
        document.documentElement.style.setProperty("--color-a", this.mode == "xtremPink" ? "mediumvioletred" : this.colorASave);
        document.documentElement.style.setProperty("--color-b", this.mode == "xtremPink" ? "hotpink" : this.colorBSave);
        document.documentElement.style.setProperty("--color-b-light", this.mode == "xtremPink" ? "hotpink" : this.colorBSave);
        document.documentElement.style.setProperty("--color-c", this.mode == "xtremPink" ? "hotpink" : this.colorBSave);
        document.documentElement.style.setProperty("--light-grey", this.mode == "xtremPink" ? "hotpink" : this.colorBSave);
        document.documentElement.style.setProperty("--grey", this.mode == "xtremPink" ? "mediumvioletred" : this.colorASave);
    }

    resetColors() {
        document.documentElement.style.setProperty("--color-a", this.colorASave);
        document.documentElement.style.setProperty("--color-b", this.colorBSave);
        document.documentElement.style.setProperty("--color-b-light", this.colorBSave);
        document.documentElement.style.setProperty("--color-c",  this.colorBSave);
        document.documentElement.style.setProperty("--light-grey", this.colorBSave);
        document.documentElement.style.setProperty("--grey", this.colorASave);
    }

    closeChildren() {
        this.dropdownMenus.forEach((dropdownMenu) => dropdownMenu.close());
    }

    public cascadingClose() {
        this.closeChildren();
    }

}