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
import { allOfEnum } from '../../../utils/app.utils';

export class EegDataset extends Dataset {
    samplingFrequency: number;
    channelCount: number;
    channelList: Channel[];
    eventList: Event[];
}

export class Channel {
    id: number;
    name: string;
    resolution: number;
    referenceUnits: string;
    referenceType: string;
    filter: Filter;
    position: Position
}

export class Event {
    id: number;
    value: string;
    sample: number;
    type: string;
}

export class Position {
    id: number;
    x: number;
    y: number;
    z: number;
}

export class Filter {
    id: number;
    lowCutOff: number;
    highCutOff: number;
    notch: number;
}