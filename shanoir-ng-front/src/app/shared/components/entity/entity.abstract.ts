import { EntityService } from "./entity.abstract.service";

export abstract class Entity {

    abstract id: number;
    
    abstract service: EntityService<Entity>;

    create(): Promise<Entity> {
        return this.service.create(this);
    }

    update(): Promise<void> {
        return this.service.update(this.id, this);
    }

    delete(): Promise<void> {
        return this.service.delete(this.id);
    }

    protected getIgnoreList() { return ['service']; }

    private replacer = (key, value) => {
        if (this[key] instanceof Date) {console.log(this[key])}
        if (this.getIgnoreList().indexOf(key) > -1) return undefined;
        else if (this[key] instanceof Date) return this.datePattern(this[key]);
        else return value;
    }

    public stringify() {
        return JSON.stringify(this, this.replacer);
    }

    private datePattern(date: Date): string {
         return date.getFullYear()
         + '-' 
         + ('0' + (date.getMonth() + 1)).slice(-2)
         + '-' 
         + ('0' + date.getDate()).slice(-2);
    }
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