import { Component, Input, ContentChildren, forwardRef, QueryList } from '@angular/core';

@Component({
    selector: 'menu-item',
    templateUrl: 'menu-item.component.html',
    styleUrls: ['menu-item.component.css']
})

export class MenuItemComponent {

    @Input() label: string;
    @Input() link: string;
    @Input() boolVar: boolean;
    @Input() icon: string;
    @ContentChildren(forwardRef(() => MenuItemComponent)) itemMenus: QueryList<MenuItemComponent>;

    public opened: boolean = false;
    public siblings: QueryList<MenuItemComponent>;
    public parent: any;
    public closeAll: () => void;

    constructor() {
    }

    ngAfterViewInit() {
        this.itemMenus.forEach((itemMenu, index) => {
            if (index!= 0) { // TODO : THE IF INDEX != 0 HAS TO BE REMOVED ONCE THE BUG IS FIXED : https://github.com/angular/angular/issues/10098
                itemMenu.siblings = this.itemMenus;
                itemMenu.parent = this;
            }
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
         this.itemMenus.forEach((itemMenu, index) => {
            if (index!= 0) itemMenu.close(); // TODO : THE IF INDEX != 0 HAS TO BE REMOVED ONCE THE BUG IS FIXED : https://github.com/angular/angular/issues/10098
        });
    }

    public closeSiblings() {
        if (this.siblings != undefined) {
            this.siblings.forEach((sibling, index) => {
                if (index!= 0) sibling.close(); // TODO : THE IF INDEX != 0 HAS TO BE REMOVED ONCE THE BUG IS FIXED : https://github.com/angular/angular/issues/10098
            });
        }
    }

    public toggle() {
        if (this.opened) this.close();
        else this.open();
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