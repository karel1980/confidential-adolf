import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {LobbyComponent} from "./lobby/lobby.component";

const routes: Routes = [{
  path: '',
  pathMatch: 'full',
  component: LobbyComponent
}];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
