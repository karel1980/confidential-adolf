import {Component, OnDestroy, OnInit} from '@angular/core';
import {WebsocketService} from "./websocket.service";
import {Subject} from 'rxjs';
import {createFeatureSelector, createSelector, Store} from "@ngrx/store";
import {goToRoom, setIdentification} from "./lobby.actions";
import {LobbyState, User} from "./lobby.reducer";
import {Router} from "@angular/router";
import {takeUntil} from "rxjs/operators";
import {Engine} from "./engine";

export interface Message { type: string; [key: string]: any; }
export interface Handler { type: string; handle: (Message, Store) => void }

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

  engine: Engine;

  onDestroy$ = new Subject();

  // used to avoid looping when there is an infinite loop of 'createroom -> identify -> createroom -> ...'
  roomCreationAttempted = false;

  constructor(private ws: WebsocketService, private store: Store, private router: Router) {
    this.engine = new Engine([
      IdentificationSuccessHandler,
      RoomCreatedHandler
    ], store, ws);
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
    this.engine.stop();
  }

  connect() {
    this.engine.connect(`ws://localhost:8080/ws/lobby`, () => {
      this.send({_type: "Identify", name: this.userName})
    });
  }

  send(msg: any) {
    this.engine.send(msg);
  }

}
