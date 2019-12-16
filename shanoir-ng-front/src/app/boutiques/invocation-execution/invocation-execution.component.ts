import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { ToolInfo } from '../tool.model';

@Component({
  selector: 'app-invocation-execution',
  templateUrl: './invocation-execution.component.html',
  styleUrls: ['./invocation-execution.component.css']
})
export class InvocationExecutionComponent implements OnInit {

  toolId: string = null

  constructor(private activatedRoute: ActivatedRoute, private breadcrumbService: BreadcrumbsService) {
    this.toolId = this.activatedRoute.snapshot.params['toolId'];
  }

  ngOnInit() {
  }

}
