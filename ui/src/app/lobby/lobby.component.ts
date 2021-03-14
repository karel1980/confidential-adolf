import {Component, OnDestroy, OnInit} from '@angular/core';
import {LobbyConnection, LobbyService} from "./lobby.service";
import {Subject, Subscription} from 'rxjs';
import {createFeatureSelector, createSelector, Store} from "@ngrx/store";
import {goToRoom, setIdentification} from "./lobby.actions";
import {LobbyState, User} from "./lobby.reducer";
import {Router} from "@angular/router";
import {takeUntil} from "rxjs/operators";

interface Message { type: string; [key: string]: any; }
interface Handler { type: string; handle: (Message, Store) => void }

interface IdentificationSuccess extends Message {
  id: string,
  name: string
}

interface RoomCreated extends Message {
  id: string
}

const IdentificationSuccessHandler = {
  type: "IdentificationSuccess",
  handle: (message: IdentificationSuccess, store: Store) =>
    store.dispatch(setIdentification({id: message.id, name: message.name}))
}

const RoomCreatedHandler = {
  type: "RoomCreated",
  handle: ({id}:RoomCreated, store: Store) =>
    store.dispatch(goToRoom({roomId: id}))
}

@Component({
  selector: 'app-lobby',
  templateUrl: './lobby.component.html',
  styleUrls: ['./lobby.component.scss']
})
export class LobbyComponent implements OnInit, OnDestroy {

  user: User = null;
  userName: string;
  handlers: Map<string, Handler>;

  onDestroy$ = new Subject();

  lobbyConnection: LobbyConnection;
  subscription: Subscription;

  // used to avoid looping when there is an infinite loop of 'createroom -> identify -> createroom -> ...'
  roomCreationAttempted = false;

  constructor(private lobbyService: LobbyService, private store: Store, private router: Router) {
    this.handlers = new Map([
      IdentificationSuccessHandler,
      RoomCreatedHandler
    ].map(h => [h.type, h]));
  }

  ngOnInit(): void {
    const lobbySelector = createFeatureSelector('lobby');
    const userSelector = createSelector(lobbySelector, (lobbyState: LobbyState) => lobbyState.user);
    const roomSelector = createSelector(lobbySelector, (lobbyState: LobbyState) => lobbyState.roomId);

    this.store.select(userSelector)
      .pipe(takeUntil(this.onDestroy$))
      .subscribe((user) => {
      this.user = user;
      if (user) {
        //TODO: set user in local storage? (check ngrx where this should be done -> in reducer? with effects?)
        if (!this.roomCreationAttempted) {
          this.roomCreationAttempted = true;
          this.send({"_type": "CreateRoom"});
        }
      }
    })

    // TODO: look into ngrx router features. For now this will do:
    this.store.select(roomSelector)
      .pipe(takeUntil(this.onDestroy$))
      .subscribe(roomId => {
      if (roomId) {
        this.router.navigate([`/room/${roomId}`]);
      }
    });
  }

  ngOnDestroy() {
    this.onDestroy$.next();
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

  send(msg: any) {
    this.lobbyConnection.send(msg);
  }

}
