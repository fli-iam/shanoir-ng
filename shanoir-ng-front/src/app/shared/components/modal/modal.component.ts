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

import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-modal',
  template: `
  <div class="modal fade" tabindex="-1" [ngClass]="{'out': !visible}">
    <div id={{modalDialogId}} class="modal-dialog" (click)="onContainerClicked($event)">
      <div class="content" [ngClass]="{'preout': !visibleAnimate}">
        <ng-content select=".app-modal-body"></ng-content>
      </div>
    </div>
  </div>
  `,
  styleUrls: ['modal.component.css'],
})
export class ModalComponent {

  @Input() modalDialogId: string;
  public visible = false;
  public visibleAnimate = false;

  constructor(){}

  public show(): void {
    this.visibleAnimate = true;
    this.visible = true;
  }

  public hide(): void {
    this.visibleAnimate = false;
    setTimeout(() => this.visible = false, 190);
  }

  public onContainerClicked(event: MouseEvent): void {
    // if ((<HTMLElement>event.target).parentElement.classList.contains('modal')) {
    //   this.hide();
    // } 
 /*   TODO (or not): Instead we should at least close only the current popup, not every opened popup */
  }
}
