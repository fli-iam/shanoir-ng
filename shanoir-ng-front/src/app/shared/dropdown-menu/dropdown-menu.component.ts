import { Component, Input, ContentChildren, forwardRef, QueryList, ViewChild, ElementRef, HostBinding, Renderer } from '@angular/core';
import { style, state, animate, transition, trigger } from '@angular/core';
import { MenuItemComponent } from '../dropdown-menu/menu-item/menu-item.component'

@Component({
    selector: 'dropdown-menu',
    templateUrl: 'dropdown-menu.component.html',
    styleUrls: ['dropdown-menu.component.css'],
    animations: [
        trigger('myAnimation', [
            state('0', style({height: '0'})),
            state('1', style({height: '*'})),
            transition(
                '0 => 1', [
                    style({opacity: 1, height: 0}),
                    animate('200ms ease-in-out', style({opacity: 1, height: '*'}))
                ]
            )
        ])
    ],
})

export class DropdownMenuComponent {

    @Input() label: string;
    @Input() link: string; 
    @ContentChildren(forwardRef(() => MenuItemComponent)) itemMenus: QueryList<MenuItemComponent>; 
    @Input() boolVar: boolean;
    @ViewChild('container') container: ElementRef;
    @Input() mode: "top" | "tree";

    public opened: boolean;
    public parent: any;

    private static documentListenerInit = false;
    private static openedMenus: Set<DropdownMenuComponent>; // every opened menu in the document (upgrade idea : named groups of menu)

    constructor(public elementRef: ElementRef, private renderer: Renderer) { 
        this.mode = "top"
        this.opened = false;
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

        this.renderer.setElementClass(this.elementRef.nativeElement, this.mode+"-mode", true);
    }

    public open() {
        //this.closeSiblings();
        this.opened =  true;
        DropdownMenuComponent.openedMenus.add(this);
    }

    public close() {
        DropdownMenuComponent.openedMenus.delete(this);
        this.opened =  false;
        this.closeChildren();
    }

    public closeChildren() {
         this.itemMenus.forEach((itemsMenu) => itemsMenu.close());
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

    public static clickDocument = (event: Event) => {
        DropdownMenuComponent.openedMenus.forEach((menu) => {
            if (!menu.container.nativeElement.contains(event.target)) {
                menu.close();
            };
        });
    }
}