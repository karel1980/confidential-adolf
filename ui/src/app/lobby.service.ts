import {Injectable} from '@angular/core';
import {Observable, Observer, Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LobbyService {

  constructor() {
  }

  connect(url: string): LobbyConnection {
    const socket = new WebSocket(url);

    const observable = Observable.create(
      (observer: Observer<MessageEvent>) => {
        socket.onmessage = observer.next.bind(observer);
        socket.onerror = observer.error.bind(observer);
        socket.onclose = observer.complete.bind(observer);
        return socket.close.bind(socket);
      }
    );

    const observer = Subject.create({
      next: (data: string) => {
        if (socket.readyState === WebSocket.OPEN) {
          socket.send(JSON.stringify(data));
        }
      }
    });

    return new LobbyConnection(observable, observer)
  }
}

export class LobbyConnection {
  observable: Observable<MessageEvent>;
  observer: Subject<string>;

  constructor(observable: Observable<MessageEvent>, observer: Subject<string>) {
    this.observable = observable;
    this.observer = observer;
  }

  public send(data: string) {
    this.observer.next(data);
  }
}
