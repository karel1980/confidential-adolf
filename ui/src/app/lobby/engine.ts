import {Store} from "@ngrx/store";
import {Handler, Message} from "./lobby.component";
import {WebsocketService, WsConnection} from "./websocket.service";
import {Subscription} from "rxjs";

export class Engine {
  wsConnection: WsConnection;
  subscription: Subscription;
  handlers: Map<string, Handler>;

  constructor(handlers: Handler[], private store: Store, private wsService: WebsocketService) {
    this.handlers = new Map(handlers.map(h => [h.type, h]));
  }

  connect(url: string, onReady: () => void) {
    this.wsConnection = this.wsService.connect(url);
    if (!!this.subscription) {
      this.subscription.unsubscribe();
    }

    this.subscription = this.wsConnection.observable.subscribe(
      (msg) => {
        this.handleMessage(JSON.parse(msg.data))
      }
    )

    if (onReady) {
      this.wsConnection.ready.subscribe(() => onReady());
    }
  }

  handleMessage(msg: Message) {
    console.log("receiving >>", msg)
    if (!msg._type) {
      console.error("received message without type", msg);
      return;
    }

    const handler = this.handlers.get(msg._type);
    if (!handler) {
      console.error("no handler for message type", msg);
      return;
    }

    handler.handle(msg, this.store)
  }

  stop() {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = undefined;
    }
  }

  send(msg: any) {
    this.wsConnection.send(msg);
  }
}
