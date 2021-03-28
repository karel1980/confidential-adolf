import {Component, OnDestroy, OnInit} from '@angular/core';
import {Engine, Message} from "../lobby/engine";
import {ErrorResponse, RoomTO, RoomState, User, Vote, PolicyTile} from "../lobby/lobby.reducer";
import {WebsocketService} from "../lobby/websocket.service";
import {createFeatureSelector, createSelector, Store} from "@ngrx/store";
import {ActivatedRoute, Router} from "@angular/router";
import {identified, setLastError, setUser, syncRoom, userAdded} from "../lobby/lobby.actions";
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
  handle: (message: RoomTO, store: Store) => {
    console.log('AAA got room state', message);
    store.dispatch(syncRoom(message))
  }
}

const ErrorResponseHandler = {
  type: "ErrorResponse",
  handle: (message: ErrorResponse, store: Store) =>
    store.dispatch(setLastError(message))
}

@Component({
  selector: 'app-room',
  templateUrl: './room.component.html',
  styleUrls: ['./room.component.scss']
})
export class RoomComponent implements OnInit, OnDestroy {

  messages: [];
  newDesign = true;

  connected = false;
  user: User;
  userName: string;
  roomId: string = null;
  engine: Engine;
  room: RoomTO;
  lastError: ErrorResponse;

  Vote = Vote;
  PolicyTile = PolicyTile;

  constructor(private ws: WebsocketService, private store: Store, private router: Router,
              private route: ActivatedRoute, private roomService: RoomService) {
    this.engine = new Engine([
      IdentificationSuccessHandler,
      UserAddedHandler,
      PongHandler,
      RoomStateHandler,
      ErrorResponseHandler
    ], store, ws);
  }

  ngOnInit() {
    const lobbySelector = createFeatureSelector('lobby');
    const userSelector = createSelector(lobbySelector, (lobbyState: RoomState) => lobbyState.user);
    const roomSelector = createSelector(lobbySelector, (lobbyState: RoomState) => lobbyState.room);
    const identifiedSelector = createSelector(lobbySelector, (lobbyState: RoomState) => lobbyState.identified);
    const errorSelector = createSelector(lobbySelector, (lobbyState: RoomState) => lobbyState.lastError);

    this.roomId = this.route.snapshot.params['roomId'];

    const userStr = localStorage.getItem('user');
    if (userStr) {
      this.user = JSON.parse(userStr);
      if (this.user.name) {
        this.roomService.checkMembership(this.roomId, this.user.id)
          .subscribe(
            (result) => {
              if (result) {
                this.connected = true;
                this.connect();
              } // else connected is false and identification form is shown
            });
      }
    }

    this.store.select(userSelector).subscribe(
      (user) => {
        console.log('setting user', user);
        this.user = user;
        this.userName = user ? user.name : '';
      }
    )

    this.store.select(roomSelector).subscribe(
      (room: RoomTO) => {
        this.room = room
      }
    )

    this.store.select(identifiedSelector).subscribe(
      (identified) => {
        if (identified) {
          this.engine.send({'_type': 'GetRoomState'});
        }
      }
    )

    this.store.select(errorSelector).subscribe(
      (lastError) => {
        if (lastError) {
          this.lastError = lastError
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

  onGetRoomStateClicked() {
    this.engine.send({"_type": "GetRoomState"});
  }

  onStartGameClicked() {
    this.engine.send({"_type": "StartGame"});
  }

  onNominateChancellor(playerId: string) {
    this.engine.send({"_type": "NominateChancellor", nominatedChancellorId: playerId})
  }

  onVoteLeadership(vote: Vote) {
    this.engine.send({"_type": "LeadershipVote", vote})
  }

  onDiscardPolicyTile(tile: PolicyTile) {
    this.engine.send({"_type": "DiscardPolicyTile", policyToDiscard: tile})
  }

  onRequestVeto() {
    this.engine.send({"_type": "RequestVeto"})
  }

  onConfirmVeto() {
    this.engine.send({"_type": "ConfirmVeto"})
  }

  onDenyVeto() {
    this.engine.send({"_type": "DenyVeto"})
  }

  onInvestigateLoyalty(targetId: string) {
    this.engine.send({"_type": "InvestigateLoyalty", targetId})
  }

  onCallSpecialElection(nextPresidentId: string) {
    this.engine.send({"_type": "CallSpecialElection", nextPresidentId})
  }

  onPolicyPeek() {
    this.engine.send({"_type": "PolicyPeek"})
  }

  onExecution(targetId: string) {
    this.engine.send({"_type": "Execution", targetId})
  }

  onToggleDesign() {
    this.newDesign = !this.newDesign;
  }

  ngOnDestroy() {
    this.engine.stop();
  }

}
