import { Descriptor } from './descriptor';
import { Parameter, ParameterDescription } from './parameter/parameter';

export let fakeDescriptor: Descriptor = {
  name: 'fake tool',
  description: 'fake tool description',
  author: 'fake author',
  'command-line': 'fake command line',
  'descriptor-url': 'fake url',
  'container-image': 'fake container',
  inputs: [],
  groups: [],
  'output-files': [],
  tags: {},
  tests: {},
  'tool-version': ''
};

export let idPrefix = 'fake id ';
export let valuePrefix = 'fake value ';
export let namePrefix = 'fake name ';
export let descriptionPrefix = 'fake description ';

let i = 0;
for(let type of ['String', 'Number', 'File', 'Flag', 'Number', 'Number']) {
  let parameterDescription = new ParameterDescription();
  parameterDescription.id = idPrefix + i;
  parameterDescription.value = valuePrefix + i;
  parameterDescription.name = namePrefix + i;
  parameterDescription.description = descriptionPrefix + i;
  parameterDescription.optional = i > 1;
  parameterDescription.type = type;

  parameterDescription.minimum = 0;
  parameterDescription['exclusive-maximum'] = 10;
  parameterDescription.integer = i < 2;
  parameterDescription.list = i > 4;

  fakeDescriptor.inputs.push(parameterDescription);
  i++;
}

fakeDescriptor.groups.push({ id: idPrefix + 0, name: namePrefix + 0, description: descriptionPrefix + 0, members: [idPrefix + 0, idPrefix + 1] });
fakeDescriptor.groups.push({ id: idPrefix + 1, name: namePrefix + 1, description: descriptionPrefix + 1, members: [idPrefix + 2, idPrefix + 3, idPrefix + 4, idPrefix + 5],  'mutually-exclusive': true });
