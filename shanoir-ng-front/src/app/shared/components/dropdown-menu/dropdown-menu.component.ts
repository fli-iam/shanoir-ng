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

import { Component, ContentChildren, ElementRef, forwardRef, HostBinding, Input, Output, QueryList, ViewChild, EventEmitter, OnChanges, SimpleChanges, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';

import { menuAnimDur, menuSlideRight } from '../../animations/animations';
import { GlobalService } from '../../services/global.service';
import { MenuItemComponent } from './menu-item/menu-item.component';

// @dynamic
@Component({
    selector: 'dropdown-menu',
    templateUrl: 'dropdown-menu.component.html',
    styleUrls: ['dropdown-menu.component.css'],
    animations: [menuSlideRight]
})
export class DropdownMenuComponent implements OnChanges, OnDestroy {

    @Input() label: string;
    @Input() awesome: string;
    @Input() link: string;
    @ContentChildren(forwardRef(() => MenuItemComponent)) itemMenus: QueryList<MenuItemComponent>;
    @Input() boolVar: boolean;

    @ViewChild('container', { static: false }) container: ElementRef;

    @HostBinding('class.opened') opened: boolean = false;
    @Input() openInput: boolean = false;
    @Output() openInputChange: EventEmitter<boolean> = new EventEmitter();
    public parent: any;
    public hasChildren: boolean = true;
    public overflow: boolean = false;
    private globalClickSubscription: Subscription;

    constructor(public elementRef: ElementRef, private globalService: GlobalService) {
        setTimeout(() => {
            this.globalClickSubscription = globalService.onGlobalClick.subscribe(clickEvent => {
                if (!this.elementRef.nativeElement.contains(clickEvent.target)) {
                    this.close();
                }
            }) 
        })
    }
    
    ngOnDestroy(): void {
        this.globalClickSubscription.unsubscribe();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.openInput) {
            if (this.openInput && !this.opened) {
                this.openAction();
            }
            else if (!this.openInput && this.opened) {
                this.close(() => {});
            }
        }
    }

    ngAfterViewInit() {
        this.itemMenus.forEach((itemMenu: MenuItemComponent) => {
            itemMenu.siblings = this.itemMenus;
            itemMenu.parent = this;
        });
    }

    public open(event: Event) {
        this.openAction();
    }

    private openAction() {
        this.opened = true;
        this.openInputChange.emit(this.opened);
        setTimeout(() => this.overflow = false, menuAnimDur);
    }

    public close(callback: () => void = () => { }) {
        if (this.hasChildren && this.opened) {
            this.closeChildren(() => {
                this.overflow = true;
                this.opened = false;
                this.openInputChange.emit(this.opened);
                setTimeout(callback, menuAnimDur);
            });
        } else {
            callback();
        }
    }

    public closeChildren(callback: () => void = () => { }) {
        if (!this.itemMenus) return;
        let menusToClose: MenuItemComponent[] = [];
        this.itemMenus.forEach((itemMenu, index) => {
            if (index != 0 && itemMenu.hasChildren && itemMenu.opened) // REMOVE index != 0 WHEN BUG FIXED
                menusToClose.push(itemMenu);
        });
        let subMenusRemaining: number = menusToClose.length;
        if (subMenusRemaining == 0) {
            callback();
            return;
        } else {
            for (let itemMenu of menusToClose) {
                itemMenu.close(() => {
                    subMenusRemaining--;
                    if (subMenusRemaining == 0) {
                        callback();
                    }
                });
            }
        }
    }

    public click() {
        if (this.link != undefined || this.boolVar == undefined) {
            this.cascadingClose();
        }
    }

    public cascadingClose() {
        if (this.parent != undefined)
            this.parent.cascadingClose();
    }
}