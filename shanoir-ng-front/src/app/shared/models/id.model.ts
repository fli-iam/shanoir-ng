export class Id {
    
    constructor(
        public id: number
    ) {}

    public static toIdList(from: any[]): Id[] {
        let list: Id[];
        for (let item of from) {
            if (item.id) list.push(new Id(item.id));
        }
        return list;
    }
}