import { allOfEnum } from "../../utils/app.utils";

export enum ImagedObjectCategory {
    PHANTOM = 'PHANTOM',
    LIVING_HUMAN_BEING = 'LIVING_HUMAN_BEING',
    HUMAN_CADAVER = 'HUMAN_CADAVER',
    ANATOMICAL_PIECE = 'ANATOMICAL_PIECE'
} 
export namespace ImagedObjectCategory {

    export function all(): Array<ImagedObjectCategory> {
        return allOfEnum<ImagedObjectCategory>(ImagedObjectCategory);
    }
}