import {Component, OnDestroy, OnInit} from '@angular/core';
import {Subject} from 'rxjs';
import {createFeatureSelector, createSelector, Store} from "@ngrx/store";
import {goToRoom, setUser} from "./lobby.actions";
import {RoomState} from "./lobby.reducer";
import {Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {takeUntil} from "rxjs/operators";

@Component({
  selector: 'app-lobby',
  templateUrl: './lobby.component.html',
  styleUrls: ['./lobby.component.scss']
})
export class LobbyComponent implements OnInit, OnDestroy {

  userName: string;

  user = {
    id: null,
    name: null,
  }

  onDestroy$ = new Subject();

  constructor(private store: Store, private router: Router, private http: HttpClient) {
  }

  ngOnInit(): void {
    const lobbySelector = createFeatureSelector('lobby');
    const roomSelector = createSelector(lobbySelector, (lobbyState: RoomState) => lobbyState.roomId);
    const userSelector = createSelector(lobbySelector, (lobbyState: RoomState) => lobbyState.user);

    // TODO: look into ngrx router features. For now this will do:
    this.store.select(roomSelector)
      .pipe(takeUntil(this.onDestroy$))
      .subscribe(roomId => {
        if (roomId) {
          this.router.navigate([`/room/${roomId}`]);
        }
      });

    this.store.select(userSelector)
      .pipe(takeUntil(this.onDestroy$))
      .subscribe(user => {
        if (user) {
          this.user = user;
          this.userName = user.name;
        }
      });
  }

  ngOnDestroy() {
    this.onDestroy$.next();
  }

  createRoom() {
    this.store.dispatch(setUser({
      id: this.user.id,
      name: this.userName
    }));
    this.http.post<{id: string}>('/api/room', {})
      .subscribe(({id}) => {
        this.store.dispatch(goToRoom({roomId: id}));
      });
  }

}
