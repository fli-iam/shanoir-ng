import { Injectable } from "@angular/core";
import { EntityService } from '../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../utils/app.utils';
import { NiftiConverter } from "./nifti.converter.model";

@Injectable()
export class NiftiConverterService extends EntityService<NiftiConverter>{

    API_URL = AppUtils.BACKEND_API_NIFTI_CONVERTER_URL;

    getEntityInstance() { return new NiftiConverter(); }
}