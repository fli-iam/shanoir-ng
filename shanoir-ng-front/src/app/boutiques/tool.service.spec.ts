import { asyncData, asyncError } from './testing/index';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { TestBed, ComponentFixture, async, fakeAsync, flushMicrotasks } from '@angular/core/testing';

import { ToolService } from './tool.service';
import { ToolInfo } from './tool.model';

describe('ToolService', () => {

  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;
  let service: ToolService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      // Import the HttpClient mocking services
      imports: [ HttpClientTestingModule ],
      // Provide the service-under-test
      providers: [ ToolService ]
    });

    // Inject the http, test controller, and service-under-test
    // as they will be referenced by each test.
    httpClient = TestBed.get(HttpClient);
    httpTestingController = TestBed.get(HttpTestingController);
    service = TestBed.get(ToolService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  describe('#getAll', () => {
    let expectedTools: ToolInfo[];

    beforeEach(() => {
      service = TestBed.get(ToolService);
      expectedTools = [
        { id: 1, name: 'A' },
        { id: 2, name: 'B' },
       ] as ToolInfo[];
    });

    it('should return expected tool', fakeAsync(() => {

      let allTools: any[] = null;

      service.getAll().then( tools => allTools = tools, fail );

      // HeroService should have made one request to GET heroes from expected URL
      const req = httpTestingController.expectOne(service.API_URL + '/all');
      expect(req.request.method).toEqual('GET');

      // Respond with the mock heroes
      req.flush(expectedTools);

      flushMicrotasks();
      expect(allTools).toEqual(expectedTools, 'should return expected tools');

    }));

    it('should be OK returning no tool', fakeAsync(() => {

      let allTools: any[] = null;

      service.getAll().then(tools => allTools = tools, fail);

      const req = httpTestingController.expectOne(service.API_URL + '/all');
      req.flush([]); // Respond with no heroes

      flushMicrotasks();

      expect(allTools.length).toEqual(0, 'should have empty tool array')

    }));

    it('should turn 404 into a user-friendly error', fakeAsync(() => {
      const message = 'Deliberate 404';
      
      let hasFailed: boolean = null;

      service.getAll().then( tools => hasFailed = false, error => hasFailed = true );

      const req = httpTestingController.expectOne(service.API_URL + '/all');

      // respond with a 404 and the error message in the body
      req.flush(message, {status: 404, statusText: 'Not Found'});

      flushMicrotasks();

      expect(hasFailed).toBeTruthy();
    }));

  });

  describe('#getDescriptor', () => {
    let expectedDescriptor = { name: 'fake descriptor'};
    let toolId = Math.random();

    beforeEach(() => {
      service = TestBed.get(ToolService);
    });

    it('should return expected tool descriptor', fakeAsync(() => {
      let descriptor = null;

      service.getDescriptor(toolId).then( d => descriptor = d, fail );

      // HeroService should have made one request to GET heroes from expected URL
      const req = httpTestingController.expectOne(`${service.API_URL}/${encodeURIComponent(toolId)}/descriptor/`);
      expect(req.request.method).toEqual('GET');

      // Respond with the mock heroes
      req.flush(expectedDescriptor);

      flushMicrotasks();
      expect(descriptor).toEqual(expectedDescriptor, 'should return expected descriptor');
    }));

    it('should turn 404 into a user-friendly error', fakeAsync(() => {
      let errorMessage: string = null;

      const message = 'Deliberate 404';
      service.getDescriptor(toolId).then( tools => errorMessage = null, error => errorMessage = error.error );

      const req = httpTestingController.expectOne(`${service.API_URL}/${encodeURIComponent(toolId)}/descriptor/`);

      // respond with a 404 and the error message in the body
      req.flush(message, {status: 404, statusText: 'Not Found'});

      flushMicrotasks();

      expect(errorMessage).toContain(message);
    }));

  });

  describe('#getInvocation', () => {
    let expectedInvocation = 'fake invocation';
    let toolId = Math.random();

    beforeEach(() => {
      service = TestBed.get(ToolService);
    });

    it('should return expected tool invocation', fakeAsync(() => {
      let invocation = null;
      service.getInvocation(toolId).then( i => invocation = i, fail );

      // HeroService should have made one request to GET heroes from expected URL
      const req = httpTestingController.expectOne(`${service.API_URL}/${encodeURIComponent(toolId)}/invocation/`);
      expect(req.request.method).toEqual('GET');

      // Respond with the mock heroes
      req.flush(expectedInvocation);

      flushMicrotasks();

      expect(invocation).toEqual(expectedInvocation, 'should return expected invocation');
    }));

    it('should turn 404 into a user-friendly error', fakeAsync(() => {
      const message = 'Deliberate 404';
      let errorMessage: string = null;

      service.getInvocation(toolId).then( invocation => errorMessage = null, error => errorMessage = error.error );

      const req = httpTestingController.expectOne(`${service.API_URL}/${encodeURIComponent(toolId)}/invocation/`);

      // respond with a 404 and the error message in the body
      req.flush(message, {status: 404, statusText: 'Not Found'});

      flushMicrotasks();

      expect(errorMessage).toContain(message);
    }));

  });

  describe('#generateCommand', () => {
    let fakeInvocation = 'fake invocation';
    let expectedCommand = 'fake command';
    let toolId = Math.random();

    beforeEach(() => {
      service = TestBed.get(ToolService);
    });

    it('should return expected generated command', fakeAsync( () => {
      let command = null;
      service.generateCommand(toolId, fakeInvocation).then( c => command = c, fail );

      // HeroService should have made one request to GET heroes from expected URL
      const req = httpTestingController.expectOne(`${service.API_URL}/${encodeURIComponent(toolId)}/generate-command/`);
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(fakeInvocation);
      expect(req.request.responseType).toEqual('text');

      // Respond with the mock heroes
      req.flush(expectedCommand);

      flushMicrotasks();
      expect(command).toEqual(expectedCommand, 'should return expected generated command');
    }));

    it('should turn 404 into a user-friendly error', fakeAsync( () => {
      const message = 'Deliberate 404';

      let errorMessage = null;
      service.generateCommand(toolId, fakeInvocation).then( c => errorMessage = null, error => errorMessage = error.error );

      const req = httpTestingController.expectOne(`${service.API_URL}/${encodeURIComponent(toolId)}/generate-command/`);

      // respond with a 404 and the error message in the body
      req.flush(message, {status: 404, statusText: 'Not Found'});

      flushMicrotasks();
      expect(errorMessage).toContain(message);
    }));

  });

  describe('#execute', () => {
    let fakeInvocation = 'fake invocation';
    let expectedOutput = 'fake output';
    let toolId = Math.random();

    beforeEach(() => {
      service = TestBed.get(ToolService);
    });

    it('should return expected resulting output', fakeAsync( () => {

      let output = null;

      service.execute(toolId, fakeInvocation).then( o => output = o, fail );

      // HeroService should have made one request to GET heroes from expected URL
      const req = httpTestingController.expectOne(`${service.API_URL}/${encodeURIComponent(toolId)}/execute/`);
      expect(req.request.method).toEqual('POST');
      expect(req.request.body).toEqual(fakeInvocation);
      expect(req.request.responseType).toEqual('text');

      // Respond with the mock heroes
      req.flush(expectedOutput);

      flushMicrotasks();
      expect(output).toEqual(expectedOutput, 'should return expected resulting output');
    }));

    it('should turn 404 into a user-friendly error', fakeAsync(() => {
      const message = 'Deliberate 404';

      let errorMessage = null;
      service.execute(toolId, fakeInvocation).then( c => errorMessage = null, error => errorMessage = error.error );

      const req = httpTestingController.expectOne(`${service.API_URL}/${encodeURIComponent(toolId)}/execute/`);

      // respond with a 404 and the error message in the body
      req.flush(message, {status: 404, statusText: 'Not Found'});

      flushMicrotasks();
      expect(errorMessage).toContain(message);
    }));

  });


});
