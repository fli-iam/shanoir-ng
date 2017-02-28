import { Component, ViewChild, ViewChildren, QueryList } from '@angular/core';
import { DropdownMenuComponent } from '../dropdown-menu/dropdown-menu.component'

@Component({
    selector: 'navbar',
    moduleId: module.id,
    templateUrl: 'navbar.component.html',
    styleUrls: ['../css/common.css', 'navbar.component.css'],
    host: {
        '(document:click)': 'closeAll($event)',
    }
})

export class NavbarComponent {

    @ViewChild('container') container;
    @ViewChildren(DropdownMenuComponent) dropdownMenus: QueryList<DropdownMenuComponent>;

    private colorASave; 
    private colorBSave;    
    public pinkMode: boolean = false;

    constructor() {
        
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
        //return this.loginService.isUserAdmin();
        return true;
    }

    togglePink() {
        this.pinkMode = !this.pinkMode;
        let colorA = this.pinkMode ? "mediumvioletred" : this.colorASave;
        let colorB = this.pinkMode ? "hotpink" : this.colorBSave;
        document.documentElement.style.setProperty("--color-a", colorA);
        document.documentElement.style.setProperty("--color-b", colorB);
    }

    closeAll(event) {
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