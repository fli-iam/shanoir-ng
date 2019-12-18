// The parameter description as given in the json descriptor file
export class ParameterDescription<T> {
  value: T
  id: string
  name: string
  description: string
  optional: boolean
  type: string
  'value-key': string
  list?: boolean
  integer?: boolean
  minimum?: number
  maximum?: number
  'exclusive-minimum'?: number
  'exclusive-maximum'?: number
}

// The parameter object, model of the formGroup (created from ParameterDescription)
export class Parameter<T> extends ParameterDescription<T> {
  
  static readonly epsilon = 0.0000001

  static readonly typeToInputType = {
    'String': 'text',
    'File': 'text',
    'Flag': 'checkbox',
    'Number': 'number',
    '': 'text'
  }

  constructor(description: ParameterDescription<T> = new ParameterDescription<T>()) {
    super()
    this.value = description.value;
    this.id = description.id || '';
    this.name = description.name || '';
    this.optional = !!description.optional;
    this.description = description.description || '';
    this.type = description.type || '';
    this.list = description.list || false;
    this.minimum = description.minimum;
    this.maximum = description.maximum;
    this['exclusive-minimum'] = description['exclusive-minimum'];
    this['exclusive-maximum'] = description['exclusive-maximum'];
    this.integer = description.integer || false;
  }

  getMinimum() {
    return this['exclusive-minimum'] != null ? (this['exclusive-minimum'] + (this.integer ? 1 : Parameter.epsilon) ) : this.minimum;
  }

  getMaximum() {
    return this['exclusive-maximum'] != null ? (this['exclusive-maximum'] - (this.integer ? 1 : Parameter.epsilon) ) : this.maximum;
  }

  getInputType() {
    return this.list ? 'text' : Parameter.typeToInputType[this.type];
  }

  parseValue(value: any) {
    if(this.list) {
      try {
        return JSON.parse('[' + value + ']');
      } catch (e) {
        console.log(e);
        return [];
      }
    }
    switch (this.type) {
      case 'Number':
        return +value;
      case 'Flag':
        return value == 'on' || value === true;
      default:
        return value;
    }
  }

  getValue(value: any) {
    return this.list ? (this.type == 'Number' ? value.join(', ') : '"' + value.join('", "') + '"') : value;
  }
}

export class StringParameter extends Parameter<string> {
  type = 'String'

  constructor(description: ParameterDescription<string> = new ParameterDescription<string>()) {
    super(description);
  }
}

export class FileParameter extends Parameter<string> {
  type = 'File'

  constructor(description: ParameterDescription<string> = new ParameterDescription<string>()) {
    super(description);
  }
}

export class NumberParameter extends Parameter<number> {
  type = 'Number'

  constructor(description: ParameterDescription<number> = new ParameterDescription<number>()) {
    super(description);
  }
}