/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
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

import { Injectable } from '@angular/core';
import { Enum } from "../../../shared/utils/enum";
import { AnestheticType } from "./anestheticType";
import { InjectionType } from "./injectionType";
import { InjectionInterval } from "./injectionInterval";
import { InjectionSite } from "./injectionSite";
import { TherapyType } from "./therapyType";
import { Frequency } from "./frequency";

@Injectable()
export class EnumUtils {
    
   //  protected static enumUtils : EnumUtils;
  
    getEnumArrayFor(enumName:string): Enum[] {
        let enumArray:Enum[] = [];
        var enumIs = null;
        switch(enumName) { 
           case 'AnestheticType': {
              enumIs = AnestheticType;
              break; 
           } 
           case 'InjectionSite': { 
              enumIs = InjectionSite;
              break; 
           }
           case 'InjectionType': { 
              enumIs = InjectionType;
              break; 
           }
           case 'InjectionInterval': { 
              enumIs = InjectionInterval;
              break; 
           }
           case 'TherapyType': {
              enumIs = TherapyType;
              break; 
           }
           case 'Frequency': {
              enumIs = Frequency;
              break; 
           } 
           default: { 
              //statements; 
              break; 
           } 
        }
        if(enumIs){
            var keys = Object.keys(enumIs);
            for (var i = 0; i < keys.length; i = i+2) {
                var enumVar: Enum = new Enum();
                enumVar.key = keys[i];
                enumVar.value = enumIs[keys[i]];
                enumArray.push(enumVar);
            }
        }            
        return enumArray;        
    }
    
    getEnumValue(enumArray:Enum[],key:string): string{
        for(let current of enumArray){
            if(current.key == key) return current.value;
        }
        return '';
    }
       
}