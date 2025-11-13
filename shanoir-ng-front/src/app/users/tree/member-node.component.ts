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
import { Component, ElementRef, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Router, RouterLink } from '@angular/router';


import { TreeNodeAbstractComponent } from 'src/app/shared/components/tree/tree-node.abstract.component';
import { TreeService } from 'src/app/studies/study/tree.service';

import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { MemberNode } from '../../tree/tree.model';
import { User } from '../shared/user.model';

import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';
import { DropdownMenuComponent } from '../../shared/components/dropdown-menu/dropdown-menu.component';
import { MenuItemComponent } from '../../shared/components/dropdown-menu/menu-item/menu-item.component';


@Component({
    selector: 'member-node',
    templateUrl: 'member-node.component.html',
    imports: [TreeNodeComponent, DropdownMenuComponent, RouterLink, MenuItemComponent]
})

export class MemberNodeComponent extends TreeNodeAbstractComponent<MemberNode> implements OnChanges {

    @Input() input: MemberNode | User;
    isAdmin: boolean;
    detailsPath: string = '/user/details/';

    constructor(
            private router: Router,
            keycloakService: KeycloakService,
            protected treeService: TreeService,
            elementRef: ElementRef) {
        super(elementRef);
        this.isAdmin = keycloakService.isUserAdmin();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof MemberNode) {
                this.node = this.input;
            } else {
                throw new Error('not implemented yet');
            }
        }
    }

    hasChildren(): boolean | 'unknown' {
        if (!this.node.rights) return false;
        else if (this.node.rights == 'UNLOADED') return 'unknown';
        else return this.node.rights.length > 0;
    }
}
