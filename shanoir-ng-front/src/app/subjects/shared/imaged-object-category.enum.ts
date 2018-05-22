export enum ImagedObjectCategory {
    PHANTOM = 'PHANTOM',
    LIVING_HUMAN_BEING = 'LIVING_HUMAN_BEING',
    HUMAN_CADAVER = 'HUMAN_CADAVER',
    ANATOMICAL_PIECE = 'ANATOMICAL_PIECE'
} 
export namespace ImagedObjectCategory {

    export function all(): Array<ImagedObjectCategory> {
        let list: Array<ImagedObjectCategory> = [];
        for (let key in ImagedObjectCategory) {
            if (key != 'all' && isNaN(Number(key)))
            list.push(eval('ImagedObjectCategory.'+key));
        }
        return list;
    }
}