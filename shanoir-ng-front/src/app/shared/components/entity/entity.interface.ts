export interface Entity {

    id: number;
    
    create(): Promise<Entity>;
    update(): Promise<void>;
    delete(): Promise<void>;
}

export class EntityRoutes {

    constructor(public routingName: string) {}

    public getRouteToView(id: number): string {
        return '/' + this.routingName + '/details/' + id;
    }

    public getRouteToEdit(id: number): string {
        return '/' + this.routingName + '/edit/' + id;
    }

    public getRouteToCreate(): string {
        return '/' + this.routingName + '/create';
    }

    public getRouteToList(): string {
        return '/' + this.routingName + '/list';
    }

}