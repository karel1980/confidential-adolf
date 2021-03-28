import {BrowserModule} from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {LobbyComponent} from './lobby/lobby.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

import {lobbyReducer} from './lobby/lobby.reducer';
import {Store, StoreModule} from "@ngrx/store";
import {RoomComponent} from './room/room.component';
import {EffectsModule} from '@ngrx/effects';
import {HttpClientModule} from "@angular/common/http";
import {v4} from 'uuid';
import {setUser} from "./lobby/lobby.actions";
import { GameComponent } from './game/game.component';
import { LiberalLaneComponent } from './liberal-lane/liberal-lane.component';
import { ElectionTrackerComponent } from './election-tracker/election-tracker.component';
import { FascistLaneComponent } from './fascist-lane/fascist-lane.component';
import { PolicyTileComponent } from './policy-tile/policy-tile.component';
import { PlayerLaneComponent } from './player-lane/player-lane.component';
import { PlayerTileComponent } from './player-tile/player-tile.component';

function createUserId(store: Store) {
  return () => {
    let user = JSON.parse(localStorage.getItem('user'));
    if (!user) {
      user = {
        id: v4(),
        name: ''
      };
    }

    store.dispatch(setUser(user));
  }
}

@NgModule({
  declarations: [
    AppComponent,
    LobbyComponent,
    RoomComponent,
    GameComponent,
    LiberalLaneComponent,
    ElectionTrackerComponent,
    FascistLaneComponent,
    PolicyTileComponent,
    PlayerLaneComponent,
    PlayerTileComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    StoreModule.forRoot({lobby: lobbyReducer}),
    EffectsModule.forRoot()
  ],
  providers: [{
    provide: APP_INITIALIZER,
    deps: [Store],
    useFactory: createUserId,
    multi: true
  }],
  bootstrap: [AppComponent]
})
export class
AppModule {
}
