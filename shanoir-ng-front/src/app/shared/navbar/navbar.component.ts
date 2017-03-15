import { Component, ViewChildren, QueryList } from '@angular/core';

import { DropdownMenuComponent } from '../dropdown-menu/dropdown-menu.component'
import { KeycloakService } from "../keycloak/keycloak.service";

@Component({
    selector: 'navbar',
    templateUrl: 'navbar.component.html',
    styleUrls: ['navbar.component.css']
})

export class NavbarComponent {

    @ViewChildren(DropdownMenuComponent) dropdownMenus: QueryList<DropdownMenuComponent>;

    private colorASave: string;
    private colorBSave: string;
    public mode: "default" | "pink" | "xtremPink" = "default";

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