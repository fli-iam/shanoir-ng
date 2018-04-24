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
    if ((<HTMLElement>event.target).parentElement.classList.contains('modal')) {
      this.hide();
    } 
 /*   TODO (or not): Instead we should at least close only the current popup, not every opened popup */
  }
}
