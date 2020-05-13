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

import { allOfEnum } from "../../utils/app.utils";

export enum ImagedObjectCategory {
    PHANTOM = 'PHANTOM',
    LIVING_HUMAN_BEING = 'LIVING_HUMAN_BEING',
    HUMAN_CADAVER = 'HUMAN_CADAVER',
    ANATOMICAL_PIECE = 'ANATOMICAL_PIECE', 
    LIVING_ANIMAL = 'LIVING_ANIMAL',
    ANIMAL_CADAVER = 'ANIMAL_CADAVER'
} 
export namespace ImagedObjectCategory {

    export function all(): Array<ImagedObjectCategory> {
        return allOfEnum<ImagedObjectCategory>(ImagedObjectCategory);
    }
    
    export function allPreClinical(preclinical : boolean): Array<ImagedObjectCategory> {
    	let allPreclinicalImagedObjectCategory: Array<ImagedObjectCategory> = new Array();
        if (preclinical){
        	allPreclinicalImagedObjectCategory.push(ImagedObjectCategory.LIVING_ANIMAL);
        	allPreclinicalImagedObjectCategory.push(ImagedObjectCategory.ANIMAL_CADAVER);
        	allPreclinicalImagedObjectCategory.push(ImagedObjectCategory.PHANTOM);
        	allPreclinicalImagedObjectCategory.push(ImagedObjectCategory.ANATOMICAL_PIECE);
        }else{
        	allPreclinicalImagedObjectCategory.push(ImagedObjectCategory.LIVING_HUMAN_BEING);
        	allPreclinicalImagedObjectCategory.push(ImagedObjectCategory.HUMAN_CADAVER);
        	allPreclinicalImagedObjectCategory.push(ImagedObjectCategory.PHANTOM);
        	allPreclinicalImagedObjectCategory.push(ImagedObjectCategory.ANATOMICAL_PIECE);
        }
        return allPreclinicalImagedObjectCategory;
    }
}