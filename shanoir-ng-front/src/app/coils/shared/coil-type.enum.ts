import { allOfEnum } from "../../utils/app.utils";

export enum CoilType {
    BODY = "BODY",
    HEAD = "HEAD",
    SURFACE = "SURFACE",
    MULTICOIL = "MULTICOIL",
    EXTREMITY = "EXTREMITY"
}

export namespace CoilType {

    export function all(): Array<CoilType> {
        return allOfEnum<CoilType>(CoilType);
    }
}