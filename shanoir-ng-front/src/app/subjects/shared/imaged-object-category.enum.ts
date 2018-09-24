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