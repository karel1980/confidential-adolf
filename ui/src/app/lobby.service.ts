import {Injectable} from '@angular/core';
import {Observable, Observer, Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LobbyService {

  public observable: Observable<MessageEvent>;
  private observer: Subject<string>;

  constructor() { }

  connect(url: string) {
    const socket = new WebSocket(url);

    this.observable = Observable.create(
      (observer: Observer<MessageEvent>) => {
        socket.onmessage = observer.next.bind(observer);
        socket.onerror = observer.error.bind(observer);
        socket.onclose = observer.complete.bind(observer);
        return socket.close.bind(socket);
      }
    );

    this.observer = Subject.create({
      next: (data: string) => {
        if (socket.readyState === WebSocket.OPEN) {
          socket.send(JSON.stringify(data));
        }
      }
    });
  }

  public send(data: string) {
    this.observer.next(data);
  }
}
