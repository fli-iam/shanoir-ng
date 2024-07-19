import { TestBed } from '@angular/core/testing';

import { ExecutionDataService } from './execution.data-service';

describe('ProcessingService', () => {
  let service: ExecutionDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ExecutionDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
