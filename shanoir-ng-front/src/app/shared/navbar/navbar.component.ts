import { Component, ViewChild, ViewChildren, QueryList, ElementRef } from '@angular/core';

import { DropdownMenuComponent } from '../dropdown-menu/dropdown-menu.component'
import { KeycloakService } from "../keycloak/keycloak.service";

@Component({
    selector: 'navbar',
    templateUrl: 'navbar.component.html',
    styleUrls: ['navbar.component.css'],
    host: {
        '(document:click)': 'closeAll($event)',
    }
})

export class NavbarComponent {

    @ViewChild('container') container: ElementRef;
    @ViewChildren(DropdownMenuComponent) dropdownMenus: QueryList<DropdownMenuComponent>;

    private colorASave: string;
    private colorBSave: string;
    public pinkMode: boolean = false;

    constructor(private keycloakService: KeycloakService) {

    }

    ngAfterViewInit() {
        this.dropdownMenus.forEach((dropdownMenu) => {
            dropdownMenu.siblings = this.dropdownMenus;
            dropdownMenu.parent = this;
        });

        this.colorASave = document.documentElement.style.getPropertyValue("--color-a");
        this.colorBSave = document.documentElement.style.getPropertyValue("--color-b");
    }

    isUserAdmin(): boolean {
        return this.keycloakService.isUserAdmin();
    }

    togglePink() {
        this.pinkMode = !this.pinkMode;
        let colorA = this.pinkMode ? "mediumvioletred" : this.colorASave;
        let colorB = this.pinkMode ? "hotpink" : this.colorBSave;
        document.documentElement.style.setProperty("--color-a", colorA);
        document.documentElement.style.setProperty("--color-b", colorB);
    }

    closeAll(event: Event) {
        if (!this.container.nativeElement.contains(event.target)) {
            this.closeChildren();
        };
    }

    closeChildren() {
        this.dropdownMenus.forEach((dropdownMenu) => dropdownMenu.close());
    }

    public cascadingClose() {
        this.closeChildren();
    }

}