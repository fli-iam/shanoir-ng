
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

import { Component, ContentChildren, forwardRef, HostListener, Input, QueryList, AfterViewInit } from '@angular/core';

@Component({
    selector: 'menu-item',
    templateUrl: 'menu-item.component.html',
    styleUrls: ['menu-item.component.css'],
    imports: []
})

export class MenuItemComponent implements AfterViewInit {

    @Input() label: string;
    @Input() boolVar: boolean;
    @Input() awesome: string;
    @Input() disabled: boolean;
    @ContentChildren(forwardRef(() => MenuItemComponent)) itemMenus: QueryList<MenuItemComponent>;

    public opened: boolean = true;
    public siblings: QueryList<MenuItemComponent>;
    public parent: any;
    public hasChildren: boolean = true;
    public init: boolean = false;

    public closeAll: () => void;

    ngAfterViewInit() {
        let doHasChildren: boolean = false;
        this.itemMenus.forEach((itemMenu, index) => {
            if (index!= 0) { // TODO : THE IF INDEX != 0 HAS TO BE REMOVED ONCE THE BUG IS FIXED : https://github.com/angular/angular/issues/10098
                itemMenu.siblings = this.itemMenus;
                itemMenu.parent = this;
                doHasChildren = true;
            }
        });

        setTimeout(() => {
            this.hasChildren = doHasChildren;
            this.opened = false;
            this.init = true;
        }, 100);
    }

    @HostListener('click', []) 
    onClick() {
        if (!this.hasChildren) this.cascadingClose();
    }

    public open() {
        this.closeSiblings(() => {
            this.opened =  true;
        })
    }

    public close(callback: () => void = () => { return; }) {
        if (this.hasChildren) {
            this.closeChildren(() => {
                this.opened =  false;
                setTimeout(callback);
            });
        } else {
            callback();
        }
    }

    private closeOpenedAmong(menus: QueryList<MenuItemComponent>, callback: () => void = () => { return; }) {
        const toBeClosed: MenuItemComponent[] = [];
        menus.forEach((menu: MenuItemComponent, index: number) => {
            if (index!= 0 && menu.hasChildren && menu.opened) {
                toBeClosed.push(menu);
            }
        });
        let remaining: number = toBeClosed.length;
        if (remaining == 0) callback();
        for (const menu of toBeClosed) {
            menu.close(() => {
                remaining--;
                if (remaining == 0) {
                    callback();
                }
            })
        }
    }

    public closeChildren(callback: () => void = () => { return;}) {
        this.closeOpenedAmong(this.itemMenus, callback);
    }

    public closeSiblings(callback: () => void) {
        this.closeOpenedAmong(this.siblings, callback);
    }

    public toggle() {
        if (this.opened) this.close();
        else this.open();
    }

    public cascadingClose() {
        this.parent?.cascadingClose();
    }
}
