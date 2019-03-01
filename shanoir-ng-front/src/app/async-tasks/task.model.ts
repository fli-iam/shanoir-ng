import { Entity } from '../shared/components/entity/entity.abstract';
import { ServiceLocator } from '../utils/locator.service';
import { TaskService } from './task.service';

export class Task extends Entity {

    id: number;
    label: string;
    startDate: Date;
    endDate: Date;
    progress: number;

    service = ServiceLocator.injector.get(TaskService);
}
