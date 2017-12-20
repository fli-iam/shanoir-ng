import { Component, ViewChildren, QueryList } from '@angular/core';

import { DropdownMenuComponent } from '../components/dropdown-menu/dropdown-menu.component'
import { KeycloakService } from '../keycloak/keycloak.service';
import { ImagesUrlUtil } from '../utils/images-url.util';

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
    private brainIconPath: string = ImagesUrlUtil.BRAIN_ICON_PATH;
    private cardiogramIconPath: string = ImagesUrlUtil.CARDIOGRAM_ICON_PATH;
    private compactDiscIconPath: string = ImagesUrlUtil.COMPACT_DISC_ICON_PATH;
    private controlsIconPath: string = ImagesUrlUtil.CONTROLS_ICON_PATH;
    private folder12IconPath: string = ImagesUrlUtil.FOLDER_12_ICON_PATH;
    private folder7IconPath: string = ImagesUrlUtil.FOLDER_7_ICON_PATH;
    private hospitalIconPath: string = ImagesUrlUtil.HOSPITAL_ICON_PATH;
    private niftiIconPath: string = ImagesUrlUtil.NIFTI_ICON_PATH;
    private neurinfoIconPath: string = ImagesUrlUtil.NEURINFO_ICON_PATH;
    private nurseIconPath: string = ImagesUrlUtil.NURSE_ICON_PATH;
    private podiumIconPath: string = ImagesUrlUtil.PODIUM_ICON_PATH;
    private shareIconPath: string = ImagesUrlUtil.SHARE_ICON_PATH;
    private shanoirIconPath: string = ImagesUrlUtil.SHANOIR_UP_TRANSP_LOGO_PATH;
    private stethoscopeIconPath: string = ImagesUrlUtil.STETHOSCOPE_ICON_PATH;
    private user5IconPath: string = ImagesUrlUtil.USER_5_ICON_PATH;
    private userIconPath: string = ImagesUrlUtil.USER_ICON_PATH;
    private users1IconPath: string = ImagesUrlUtil.USERS_1_ICON_PATH;
    private usersIconPath: string = ImagesUrlUtil.USERS_ICON_PATH;
    private wifiIconPath: string = ImagesUrlUtil.WIFI_ICON_PATH;
    private xRay1IconPath: string = ImagesUrlUtil.X_RAY_1_ICON_PATH;

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

    isUserGuest(): boolean {
        return this.keycloakService.isUserGuest();
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