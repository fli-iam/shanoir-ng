import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ToolInfo as FakeToolInfo } from '../testing/tool.model'
import { ToolInfo } from '../tool.model'

import { ToolDescriptorInfoComponent } from './tool-descriptor-info.component';

describe('ToolDescriptorInfoComponent', () => {
  let component: ToolDescriptorInfoComponent;
  let fixture: ComponentFixture<ToolDescriptorInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ToolDescriptorInfoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ToolDescriptorInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not display anything if tool is null', () => {
    const toolNameSpan: HTMLSpanElement = fixture.nativeElement.querySelector('#tool-name');
    expect(toolNameSpan).toBe(null);
    const toolDescriptionSpan: HTMLSpanElement = fixture.nativeElement.querySelector('#tool-description');
    expect(toolDescriptionSpan).toBe(null);
    const toolTagsSpan: HTMLSpanElement = fixture.nativeElement.querySelector('#tool-tags');
    expect(toolTagsSpan).toBe(null);
  });

  it('should display info if tool is defined', () => {
    const fakeToolInfo:ToolInfo = new FakeToolInfo() as ToolInfo;
    component.descriptor = fakeToolInfo;
    component.descriptor.name = 'fake tool';
    component.descriptor.description = 'fake description';
    component.descriptor.tags = 'fake tags';
    fixture.detectChanges();

    const toolNameSpan: HTMLSpanElement = fixture.nativeElement.querySelector('#tool-name');
    expect(toolNameSpan.textContent).toBe(component.descriptor.name);
    const toolDescriptionSpan: HTMLSpanElement = fixture.nativeElement.querySelector('#tool-description');
    expect(toolDescriptionSpan.textContent).toBe(component.descriptor.description);
    const toolTagsSpan: HTMLSpanElement = fixture.nativeElement.querySelector('#tool-tags');
    expect(toolTagsSpan.textContent).toBe(component.getTags());
  });


});
