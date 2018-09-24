export interface Entity {

    id: number;
    create(): Promise<Entity>;
    update(): Promise<void>;
    delete(): void;

}