import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LobbyComponent} from "./lobby/lobby.component";
import {RoomComponent} from "./room/room.component";

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: LobbyComponent
  },
  {
    path: 'room/:roomId',
    pathMatch: 'prefix',
    component: RoomComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {relativeLinkResolution: 'legacy'})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
