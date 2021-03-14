import {Component, OnInit} from '@angular/core';
import {LobbyConnection, LobbyService} from "../lobby.service";
import {Subscription} from 'rxjs';
import {createFeatureSelector, createSelector, Store} from "@ngrx/store";
import {setIdentification} from "./lobby.actions";
import {LobbyState, User} from "./lobby.reducer";

type Message = { type: string; [key: string]: any; }
type Handler = { type: string; handle: (Message, store: Store) => void };

interface IdentificationSuccess extends Message {
  id: string,
  name: string
}

const TalkHandler = {
  type: "UserTalked",
  handle: function (message: Message, store: Store) {
    console.log("todo: handle message")
  }
}

const IdentificationSuccessHandler = {
  type: "IdentificationSuccess",
  handle: (message: IdentificationSuccess, store: Store) => {
    console.log("dispatching setIdentification");
    store.dispatch(setIdentification(message))
  }
}

@Component({
  selector: 'app-lobby',
  templateUrl: './lobby.component.html',
  styleUrls: ['./lobby.component.scss']
})
export class LobbyComponent implements OnInit {

  messages = [];

  msg: string;
  user: User = null;
  userName: string;
  handlers: Map<string, Handler>;

  lobbyConnection: LobbyConnection;
  subscription: Subscription;

  constructor(private lobbyService: LobbyService, private store: Store) {
    this.handlers = new Map([
      TalkHandler,
      IdentificationSuccessHandler
    ].map(h => [h.type, h]));
  }

  ngOnInit(): void {
    const lobbySelector = createFeatureSelector('lobby');
    const userSelector = createSelector(lobbySelector, (lobbyState: LobbyState) => lobbyState.user);

    this.store.select(userSelector, u => u).subscribe((u: User) => {
      this.user = u;
    })
  }

  connect() {
    this.lobbyConnection = this.lobbyService.connect(`ws://localhost:8080/ws/lobby`);

    if (!!this.subscription) {
      this.subscription.unsubscribe();
    }

    this.subscription = this.lobbyConnection.observable.subscribe(
      (msg) => {
        this.handleMessage(JSON.parse(msg.data))
      }
    )

    this.lobbyConnection.ready.subscribe(() =>
      this.send({_type: "Identify", name: this.userName}))
  }

  handleMessage(msg: Message) {
    console.log("receiving >>", msg)
    this.messages.push(msg);
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

  onSendClicked() {
    this.send({
      "_type": "Talk",
      message: this.msg
    });
  }

  send(msg: any) {
    console.log('sending >>', msg)
    this.lobbyConnection.send(msg);
  }

}
