import {Entity} from "../../shared/components/entity/entity.abstract";
import {Subject} from "../../subjects/shared/subject.model";

export class FileEntity {


    constructor(name: string, subjectName: string, entity: Entity, uri: string) {
        this.name = name;
        this.subjectName = subjectName;
        this.entity = entity;
        this.uri = uri;
    }

    name: string;
    subjectName: string;
    entity: Entity;
    uri: string;
}

