import { Component, Input, ContentChildren, forwardRef, QueryList, ViewChild, ElementRef, HostBinding, Renderer } from '@angular/core';
import { style, state, animate, transition, trigger } from '@angular/core';
import { MenuItemComponent } from '../dropdown-menu/menu-item/menu-item.component'
import { Observable } from 'rxjs/Rx';


export const animDur: number = 100;

@Component({
    selector: 'dropdown-menu',
    templateUrl: 'dropdown-menu.component.html',
    styleUrls: ['dropdown-menu.component.css'],
    animations: [trigger('slideDown', [
        transition(
            ':enter', [
                style({height: 0}),
                animate(animDur+'ms ease-in-out', style({height: '*', 'padding-bottom': '*'}))
            ]
        ),
        transition(
            ':leave', [
                style({height: '*'}),
                animate(animDur+'ms ease-in-out', style({height: 0, 'padding-bottom': '0'}))
            ]
        )
    ])]
})
export class DropdownMenuComponent {

    @Input() label: string;
    @Input() link: string; 
    @ContentChildren(forwardRef(() => MenuItemComponent)) itemMenus: QueryList<MenuItemComponent>; 
    @Input() boolVar: boolean;
    @ViewChild('container') container: ElementRef;
    @Input() mode: "top" | "tree";

    public opened: boolean = true;
    public parent: any;
    private hasChildren: boolean = true;
    private overflow: boolean = false;
    private init: boolean = false;

    private static documentListenerInit = false;
    private static openedMenus: Set<DropdownMenuComponent>; // every opened menu in the document (upgrade idea : named groups of menu)

    constructor(public elementRef: ElementRef, private renderer: Renderer) { 
        this.mode = "top"
        DropdownMenuComponent.openedMenus = new Set<DropdownMenuComponent>();

        if (!DropdownMenuComponent.documentListenerInit) {
            DropdownMenuComponent.documentListenerInit = true;
            document.addEventListener('click', DropdownMenuComponent.clickDocument.bind(this));
        }
    }

    ngAfterViewInit() { 
        this.itemMenus.forEach((itemMenu: MenuItemComponent) => {
            itemMenu.siblings = this.itemMenus;
            itemMenu.parent = this;
        });
        this.hasChildren = this.itemMenus.length > 0;

        let subscription = Observable.timer(0,100).subscribe (t=> {
            this.opened = false;
            this.overflow = true;
            this.init = true;
            subscription.unsubscribe();
        });

        this.renderer.setElementClass(this.elementRef.nativeElement, this.mode+"-mode", true);
    }

    public open() {
        //this.closeSiblings();
        if (DropdownMenuComponent.openedMenus.size > 0) {
            setTimeout(() => this.openAction(), animDur);
        } else {
            this.openAction();
        }
    }

    private openAction() {
        this.opened =  true;
        DropdownMenuComponent.openedMenus.add(this);
        setTimeout(() => this.overflow = false, animDur);
    }

    public close() {
        this.overflow = true;
        DropdownMenuComponent.openedMenus.delete(this);
        this.opened =  false;
        this.closeChildren();
    }

    public closeChildren() {
         this.itemMenus.forEach((itemsMenu) => itemsMenu.close());
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

    public static clickDocument = (event: Event) => {
        DropdownMenuComponent.openedMenus.forEach((menu) => {
            if (!menu.container.nativeElement.contains(event.target)) {
                menu.close();
            };
        });
    }
}