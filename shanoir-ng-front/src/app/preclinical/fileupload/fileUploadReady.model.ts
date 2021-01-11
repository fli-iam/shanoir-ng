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

import { Observable } from 'rxjs';

export class FileUploadReady {
    xhr:XMLHttpRequest;
    formData:FormData;
    filename:string
    
    launchRequest(url:string) : Observable<any>{
        return Observable.create(observer => {
              this.xhr.onreadystatechange = () => {
                if (this.xhr.readyState === 4) {
                  if (this.xhr.status === 200) {
                    observer.next(JSON.parse(this.xhr.response));
                    observer.complete();
                  } else {
                    observer.error(this.xhr.response);
                  }
                }
              };
              this.xhr.open('POST', url, true);
              this.xhr.send(this.formData);
        });
    }
}