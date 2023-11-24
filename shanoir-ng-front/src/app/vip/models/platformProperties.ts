/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2022 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

export enum SupportedModulesEnum {
  PROCESSING,
  DATA,
  ADVANCEDDATA,
  MANAGEMENT,
  COMMERCIAL
}

export interface PlatformProperties {

  supportedModules: Array<SupportedModulesEnum>;
  defaultLimitListExecutions: number;
  email: string;
  platformDescription: string;
  minAuthorizedExecutionTimeout: number;
  maxAuthorizedExecutionTimeout: number;
  defaultExecutionTimeout: number;
  unsupportedMethods: Array<string>;
  studiesSupport: boolean;
  defaultStudy: string;
  supportedAPIVersion: string;
  ssupportedPipelineProperties: Array<string>;


}