import {Component, OnDestroy, OnInit} from '@angular/core';
import {Engine} from "../lobby/engine";
import {User} from "../lobby/lobby.reducer";
import {WebsocketService} from "../lobby/websocket.service";
import {Store} from "@ngrx/store";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-room',
  templateUrl: './room.component.html',
  styleUrls: ['./room.component.scss']
})
export class RoomComponent implements OnInit, OnDestroy {

  user: User = null;
  userName: string;
  engine: Engine;

  constructor(private ws: WebsocketService, private store: Store, private router: Router,
              private route: ActivatedRoute) {
    this.engine = new Engine([
      /*TODO: create handlers for room messages*/
    ], store, ws);
  }

  ngOnInit() {
    const user = localStorage.getItem('user');
    if (user != null) {
      const url = this.route.snapshot.params['roomId'];
      this.engine.connect(url, () => {
        //TODO: send identification for this room
      });
    } else {
      // go back to the lobby
      this.router.navigate(['/']);
    }
  }

  ngOnDestroy() {
    this.engine.stop();
  }
}
