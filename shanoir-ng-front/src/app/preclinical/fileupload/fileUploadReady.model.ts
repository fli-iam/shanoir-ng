import { Observable } from 'rxjs/Observable';

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