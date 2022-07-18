import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CarminDatasetProcessingsComponent } from './carmin-dataset-processings.component';

describe('CarminDatasetProcessingsComponent', () => {
  let component: CarminDatasetProcessingsComponent;
  let fixture: ComponentFixture<CarminDatasetProcessingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CarminDatasetProcessingsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CarminDatasetProcessingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
