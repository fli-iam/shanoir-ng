import { Component, Input, ContentChildren, forwardRef, QueryList} from '@angular/core';
import { MenuItemComponent } from '../dropdown-menu/menu-item/menu-item.component'

@Component({
    selector: 'dropdown-menu',
    templateUrl: 'dropdown-menu.component.html',
    styleUrls: ['dropdown-menu.component.css']
})

export class DropdownMenuComponent {

    @Input() label: string;
    @Input() link: string; 
    @ContentChildren(forwardRef(() => MenuItemComponent)) itemMenus: QueryList<MenuItemComponent>; 
    @Input() boolVar: boolean;

    public opened: boolean;
    public siblings: QueryList<DropdownMenuComponent>;
    public parent: any;

    constructor() { 
    }

    ngAfterViewInit() {
        this.itemMenus.forEach((itemMenu: MenuItemComponent) => {
            itemMenu.siblings = this.itemMenus;
            itemMenu.parent = this;
        });
    }

    public open() {
        this.closeSiblings();
        this.opened =  true;
    }

    public close() {
        this.opened =  false;
        this.closeChildren();
    }

    public closeChildren() {
         this.itemMenus.forEach((itemsMenu) => itemsMenu.close());
    }

    public closeSiblings() {
        if (this.siblings != undefined) {
            this.siblings.forEach((sibling) => sibling.close());
        }
    }

    public toggle() {
        if (this.opened) {
            this.close();
        } else {
            this.open();
        }
    }

    public click() {
        if (this.link != undefined || this.boolVar == undefined) {
            this.cascadingClose();
        } 
    }

    public cascadingClose() {
        this.parent.cascadingClose();
    }

}