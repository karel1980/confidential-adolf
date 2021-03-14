import {Injectable} from '@angular/core';
import {Observable, Observer, Subject} from "rxjs";
import {take} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class LobbyService {

  constructor() {
  }

  connect(url: string): LobbyConnection {
    const socket = new WebSocket(url);

    const ready = new Subject<void>();

    const observable = Observable.create(
      (observer: Observer<MessageEvent>) => {
        socket.onmessage = observer.next.bind(observer);
        socket.onerror = observer.error.bind(observer);
        socket.onclose = observer.complete.bind(observer);
        socket.onopen = () => ready.next();
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

    return new LobbyConnection(observable, observer, ready.pipe(take(1)));
  }
}

export class LobbyConnection {
  observable: Observable<MessageEvent>;
  observer: Subject<any>;
  ready: Observable<void>;

  constructor(observable: Observable<MessageEvent>, observer: Subject<string>, ready: Observable<void>) {
    this.observable = observable;
    this.observer = observer;
    this.ready = ready;
  }

  public send(data: any) {
    this.observer.next(data);
  }
}
