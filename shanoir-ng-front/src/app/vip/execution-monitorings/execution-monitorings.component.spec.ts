import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExecutionMonitoringsComponent } from './execution-monitorings.component';

describe('ExecutionMonitoring', () => {
    let component: ExecutionMonitoringsComponent;
    let fixture: ComponentFixture<ExecutionMonitoringsComponent>;

    beforeEach(async () => {
      await TestBed.configureTestingModule({
    imports: [ExecutionMonitoringsComponent]
})
      .compileComponents();
    });

    beforeEach(() => {
      fixture = TestBed.createComponent(ExecutionMonitoringsComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });
});
