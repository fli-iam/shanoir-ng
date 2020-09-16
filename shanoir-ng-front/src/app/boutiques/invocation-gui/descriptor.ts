import { Parameter, ParameterDescription } from './parameter/parameter'
import { ParameterGroup, ParameterGroupDescription } from './parameter-group/parameter-group'

export class OutputFile {
  id: string
  name: string
  description: string
  optional: boolean
  'path-template': string
}

export class Descriptor {
  name: string
  description: string
  author: string
  'command-line': string
  'descriptor-url': string
  'container-image': string
  inputs: ParameterDescription<any>[]
  groups: ParameterGroupDescription[]
  'output-files': OutputFile[]
  tags: {} | []
  tests: {}
  'tool-version': string
}