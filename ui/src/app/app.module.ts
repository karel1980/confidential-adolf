import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {LobbyComponent} from './lobby/lobby.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

import {lobbyReducer} from './lobby/lobby.reducer';
import {StoreModule} from "@ngrx/store";

@NgModule({
  declarations: [
    AppComponent,
    LobbyComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    StoreModule.forRoot({ lobby: lobbyReducer })
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
