import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { InvocationExecutionComponent } from './invocation-execution.component';

describe('InvocationExecutionComponent', () => {
  let component: InvocationExecutionComponent;
  let fixture: ComponentFixture<InvocationExecutionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ InvocationExecutionComponent ]
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
