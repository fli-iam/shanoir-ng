import { Reference } from '../../reference/shared/reference.model';
import { ContrastAgentService } from './contrastAgent.service';
import { ServiceLocator } from "../../../utils/locator.service";
import { Entity } from "../../../shared/components/entity/entity.abstract";

export class ContrastAgent extends Entity{
  id: number;
  name: Reference;
  manufactured_name: string;
  concentration: number;
  concentration_unit: Reference;
  dose: number;
  dose_unit: Reference;
  injection_interval: Reference;
  injection_site: Reference;
  injection_type: Reference;

  service: ContrastAgentService = ServiceLocator.injector.get(ContrastAgentService);
}

