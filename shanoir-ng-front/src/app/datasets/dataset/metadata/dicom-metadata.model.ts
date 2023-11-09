import {Entity} from "../../../shared/components/entity/entity.abstract";

export class DicomMetadata extends Entity {
    id: number;
    tag: string;
    keyword: string;
    value: string;
}
