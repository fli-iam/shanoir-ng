import { Parameter, ParameterDescription } from '../parameter/parameter'

// The parameter group description as given in the json descriptor file
export class ParameterGroupDescription {
  id: string
  name: string
  description: string
  optional?: boolean
  'mutually-exclusive'?: boolean
  members: string[]
}

// The group parameter object, model of the formGroup (created from ParameterGroupDescription)
export class ParameterGroup extends ParameterGroupDescription {
  exclusive: boolean
  parameters: Parameter<any>[]

  constructor(description: ParameterGroupDescription = new ParameterGroupDescription()) {
    super();
    this.id = description.id || '';
    this.name = description.name || '';
    this.description = description.description || '';
    this.optional = description.optional != null ? description.optional : true;
    this.exclusive = description['mutually-exclusive'] || false;
    this.parameters = [];
  }
}
