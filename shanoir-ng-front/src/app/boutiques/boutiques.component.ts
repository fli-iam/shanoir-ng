import { Component, ElementRef, ViewContainerRef, AfterViewInit, ViewChild } from '@angular/core';
import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { ModalService } from '../shared/components/modals/modal.service';
import { ServiceLocator } from '../utils/locator.service';
import { InvocationComponent } from './invocation/invocation.component';
import { ExecutionComponent } from './execution/execution.component';
import { ToolInfo } from './tool.model';
import { Router } from '../breadcrumbs/router';

@Component({
  selector: 'boutiques',
  templateUrl: './boutiques.component.html',
  styleUrls: ['./boutiques.component.css']
})
export class BoutiquesComponent {

  constructor(private router: Router, private breadcrumbService: BreadcrumbsService) {
  }

  ngAfterViewInit() {
  }

  onToolSelected(toolInfo: ToolInfo) {
    // Create a breacrumbd step and go to tool url when a tool is selected
    this.breadcrumbService.nameStep(toolInfo.name);
    this.router.navigate(['boutiques/' + toolInfo.id]);
  }
}
