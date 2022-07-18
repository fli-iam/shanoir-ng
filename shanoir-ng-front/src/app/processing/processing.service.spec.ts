import { TestBed } from '@angular/core/testing';

import { ProcessingService } from './processing.service';

describe('ProcessingService', () => {
  let service: ProcessingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProcessingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
