import {Component, OnDestroy, OnInit} from '@angular/core';
import {Engine, Message} from "../lobby/engine";
import {LobbyState, Room, User} from "../lobby/lobby.reducer";
import {WebsocketService} from "../lobby/websocket.service";
import {createFeatureSelector, createSelector, Store} from "@ngrx/store";
import {ActivatedRoute, Router} from "@angular/router";
import {identified, setUser, syncRoom, userAdded} from "../lobby/lobby.actions";
import {RoomService} from "./room.service";

interface IdentificationSuccess extends Message {
  id: string,
  name: string
}

interface UserAdded extends Message {
  user: {
    id: string,
    name: string
  }
}

interface Pong {
}

const IdentificationSuccessHandler = {
  type: "IdentificationSuccess",
  handle: (message: IdentificationSuccess, store: Store) =>
    store.dispatch(identified(message))
}

const UserAddedHandler = {
  type: "UserAdded",
  handle: (message: UserAdded, store: Store) =>
    store.dispatch(userAdded(message.user))
}

const PongHandler = {
  type: "Pong",
  handle: (message: Pong, store: Store) =>
    console.log("received pong")
}

const RoomStateHandler = {
  type: "RoomState",
  handle: (message: Room, store: Store) =>
    store.dispatch(syncRoom(message))
}

@Component({
  selector: 'app-room',
  templateUrl: './room.component.html',
  styleUrls: ['./room.component.scss']
})
export class RoomComponent implements OnInit, OnDestroy {

  messages: [];

  connected = false;
  user: User;
  userName: string;
  roomId: string = null;
  engine: Engine;
  room: Room;

  constructor(private ws: WebsocketService, private store: Store, private router: Router,
              private route: ActivatedRoute, private roomService: RoomService) {
    this.engine = new Engine([
      IdentificationSuccessHandler,
      UserAddedHandler,
      PongHandler,
      RoomStateHandler
    ], store, ws);
  }

  ngOnInit() {
    const lobbySelector = createFeatureSelector('lobby');
    const userSelector = createSelector(lobbySelector, (lobbyState: LobbyState) => lobbyState.user);
    const roomSelector = createSelector(lobbySelector, (lobbyState: LobbyState) => lobbyState.room);
    const identifiedSelector = createSelector(lobbySelector, (lobbyState: LobbyState) => lobbyState.identified);

    this.roomId = this.route.snapshot.params['roomId'];

    const userStr = localStorage.getItem('user');
    if (userStr) {
      this.user = JSON.parse(userStr);
      this.roomService.checkMembership(this.roomId, this.user.id)
        .subscribe(
          (result) => {
            if (result) {
              this.connected = true;
              this.connect();
            } // else connected is false and identfication form is shown
          });
    }

    this.store.select(userSelector).subscribe(
      (user) => {
        console.log('setting user', user);
        this.user = user;
        this.userName = user ? user.name : '';
      }
    )

    this.store.select(roomSelector).subscribe(
      (room: Room) => {
        this.room = room
      }
    )

    this.store.select(identifiedSelector).subscribe(
      (identified) =>{
        if (identified) {
          this.engine.send({'_type': 'GetRoomState'});
        }
      }
    )
  }

  onClickJoin() {
    this.store.dispatch(setUser({
      id: this.user.id,
      name: this.userName
    }));
    this.connect();
  }

  connect() {
    const url = `ws://localhost:8080/ws/room/${this.roomId}`
    this.engine.connect(url, () => {
      this.engine.send({_type: "Identify", id: this.user.id, name: this.user.name})
    });
  }

  onPingClicked() {
    this.engine.send({"_type": "Ping"});
  }

  ngOnDestroy() {
    this.engine.stop();
  }

}
