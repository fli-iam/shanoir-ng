/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2022 Inria - https://www.inria.fr/
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
import { Pipeline } from './pipeline';

/**
 * One selectable version inside a PipelineGroup, with its label resolved once
 * so that the view never has to parse the identifier again.
 */
export interface PipelineVersion {
    /** Version as displayed in the picker, e.g. "1.3". */
    label: string;
    pipeline: Pipeline;
}

/**
 * All the versions VIP exposes for a single pipeline name, gathered under one tile.
 */
export interface PipelineGroup {
    /** Name shared by every version, e.g. "landmarkDetection". */
    name: string;
    /** Available versions, latest first. */
    versions: PipelineVersion[];
}
