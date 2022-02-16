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
import { Entity } from '../shared/components/entity/entity.abstract';

export class Tag extends Entity {

    id: number;
    color: string;
    name: string;

    public static makeTag(id: number, name: string, color: string, studyId: number): Tag {
        let tag = new Tag();
        tag.id = id;
        tag.name = name;
        tag.color = color;
        return tag;
    }
    
}