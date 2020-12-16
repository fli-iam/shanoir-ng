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

import { Dataset } from "../../shared/dataset.model";
import { DatasetDTO } from "../../shared/dataset.dto";
import { allOfEnum } from '../../../utils/app.utils';

export class EegDataset extends Dataset {
    samplingFrequency: number;
    channelCount: number;
    name: string;
    files: string[];
    channels: Channel[];
    events: Event[];
    coordinatesSystem: string;
}

export class EegDatasetDTO extends DatasetDTO {
    samplingFrequency: number;
    channelCount: number;
    name: string;
    files: string[];
    channels: Channel[];
    events: Event[];
    coordinatesSystem: string;
    
    stringify() {
        
    }
  }

/** Represents a brainvision EEG channel */
export class Channel {
    name: string;
    resolution: number;
    referenceUnits: string;
    lowCutoff: number;
    highCutoff: number;
    notch: number;
    x: number;
    y: number;
    z: number;
}

/** Represents a brainvision event */
export class Event {
    type: string;
    description: string;
    position: string;
    points: number;
    channelNumber: number;
    date: Date;
}