import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {LobbyComponent} from './lobby/lobby.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

import {lobbyReducer} from './lobby/lobby.reducer';
import {StoreModule} from "@ngrx/store";
import {RoomComponent} from './room/room.component';
import {EffectsModule} from '@ngrx/effects';

@NgModule({
  declarations: [
    AppComponent,
    LobbyComponent,
    RoomComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    StoreModule.forRoot({ lobby: lobbyReducer }),
    EffectsModule.forRoot()
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
