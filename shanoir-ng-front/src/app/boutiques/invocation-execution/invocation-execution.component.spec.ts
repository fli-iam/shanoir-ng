import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { BreadcrumbsService as FakeBreadcrumbsService } from '../testing/breadcrumbs.service';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { ActivatedRoute } from '@angular/router';

import { InvocationExecutionComponent } from './invocation-execution.component';
import { InvocationComponent } from '../invocation/invocation.component';
import { ExecutionComponent } from '../execution/execution.component';

@Component({ selector: 'invocation', template: '', providers: [{ provide: InvocationComponent, useClass: InvocationStubComponent }] })
class InvocationStubComponent {
  @Input() toolId: any = null
}

@Component({ selector: 'execution', template: '', providers: [{ provide: ExecutionComponent, useClass: ExecutionStubComponent }] })
class ExecutionStubComponent {
  @Input() toolId: any = null
}

describe('InvocationExecutionComponent', () => {
  let component: InvocationExecutionComponent;
  let fixture: ComponentFixture<InvocationExecutionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ InvocationExecutionComponent, InvocationStubComponent, ExecutionStubComponent ],
      providers: [
        { provide: ActivatedRoute, useValue: { params: of({id: 123}) } }, 
        { provide: BreadcrumbsService, useClass: FakeBreadcrumbsService }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InvocationExecutionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
