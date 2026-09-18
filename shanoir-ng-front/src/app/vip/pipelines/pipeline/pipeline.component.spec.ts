/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Pipeline } from '@app/vip/models/pipeline';
import { PipelineGroup } from '@app/vip/models/pipeline-group';

import { PipelineComponent } from './pipeline.component';

function pipeline(name: string, version: string): Pipeline {
  return { identifier: name + '/' + version, name: name, version: version, properties: {} };
}

describe('PipelineComponent', () => {
  let component: PipelineComponent;
  let fixture: ComponentFixture<PipelineComponent>;

  const group: PipelineGroup = {
    name: 'landmarkDetection',
    versions: [
      { label: '0.8', pipeline: pipeline('landmarkDetection', '0.8') },
      { label: '0.3', pipeline: pipeline('landmarkDetection', '0.3') },
      { label: '0.2', pipeline: pipeline('landmarkDetection', '0.2') }
    ]
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
    imports: [PipelineComponent]
})
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PipelineComponent);
    component = fixture.componentInstance;
  });

  /** Inputs must be set before the first change detection, which is what runs ngOnInit. */
  function init(withGroup: PipelineGroup): void {
    component.group = withGroup;
    fixture.detectChanges();
  }

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should render nothing until a group is given', () => {
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.pipeline')).toBeNull();
  });

  it('should show one tile per name and default to the first (latest) version', () => {
    init(group);

    expect(fixture.nativeElement.querySelectorAll('.pipeline').length).toBe(1);
    expect(fixture.nativeElement.querySelector('.name').textContent.trim()).toBe('landmarkDetection');
    expect(component.pickedVersion.label).toBe('0.8');
  });

  it('should offer every version of the group in the picker', () => {
    init(group);

    const options: HTMLOptionElement[] = Array.from(fixture.nativeElement.querySelectorAll('.version-picker option'));
    expect(options.map(option => option.value)).toEqual(['0.8', '0.3', '0.2']);
  });

  it('should show a plain label instead of a picker when there is a single version', () => {
    init({ name: 'Sienax', versions: [{ label: '1.3', pipeline: pipeline('Sienax', '1.3') }] });

    expect(fixture.nativeElement.querySelector('.version-picker')).toBeNull();
    expect(fixture.nativeElement.querySelector('.version').textContent.trim()).toBe('v1.3');
  });

  it('should emit the picked version when the tile is clicked', () => {
    init(group);
    const emitted: Pipeline[] = [];
    component.pipelineSelected.subscribe(selected => emitted.push(selected));

    fixture.nativeElement.querySelector('.pipeline').click();

    expect(emitted.map(selected => selected.identifier)).toEqual(['landmarkDetection/0.8']);
  });

  it('should emit the newly picked version when the picker changes', () => {
    init(group);
    const emitted: Pipeline[] = [];
    component.pipelineSelected.subscribe(selected => emitted.push(selected));

    component.pickVersion('0.3');

    expect(component.pickedVersion.label).toBe('0.3');
    expect(emitted.map(selected => selected.identifier)).toEqual(['landmarkDetection/0.3']);
  });

  it('should keep the current version when the picker is given an unknown label', () => {
    init(group);

    component.pickVersion('does-not-exist');

    expect(component.pickedVersion.label).toBe('0.8');
  });

  it('should only look selected when the selected identifier is the picked version', () => {
    init(group);

    component.selectedIdentifier = 'landmarkDetection/0.8';
    expect(component.selected).toBeTrue();

    // another version of the same group is selected elsewhere
    component.selectedIdentifier = 'landmarkDetection/0.3';
    expect(component.selected).toBeFalse();

    // another pipeline entirely
    component.selectedIdentifier = 'Sienax/1.3';
    expect(component.selected).toBeFalse();
  });
});
