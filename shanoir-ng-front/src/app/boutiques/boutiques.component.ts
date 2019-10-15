import { Component, ElementRef, ViewContainerRef, AfterViewInit, ViewChild } from '@angular/core';
import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { ModalService } from '../shared/components/modals/modal.service';
import { ServiceLocator } from '../utils/locator.service';
import { InvocationComponent } from './invocation/invocation.component';
import { ExecutionComponent } from './execution/execution.component';
import { ToolInfo } from './tool.model';

@Component({
  selector: 'boutiques',
  templateUrl: './boutiques.component.html',
  styleUrls: ['./boutiques.component.css']
})
export class BoutiquesComponent {

  @ViewChild(InvocationComponent, {static: false})
  invocationComponent: InvocationComponent;

  @ViewChild(ExecutionComponent, {static: false})
  executionComponent: ExecutionComponent;

  constructor() {
  }

  ngAfterViewInit() {
  }

  onToolSelected(toolInfo: ToolInfo) {
    this.invocationComponent.onToolSelected(toolInfo);
    this.executionComponent.onToolSelected(toolInfo);
  }
}
