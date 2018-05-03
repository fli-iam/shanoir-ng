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
    
    protected static enumUtils : EnumUtils;
  
    getEnumArrayFor(enumName:string): Enum[] {
        let enumArray:Enum[] = [];
        var enumIs; 
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