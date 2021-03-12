import {Component, OnInit} from '@angular/core';
import {LobbyConnection, LobbyService} from "../lobby.service";
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-lobby',
  templateUrl: './lobby.component.html',
  styleUrls: ['./lobby.component.scss']
})
export class LobbyComponent implements OnInit {

  messages = [];

  msg: string;
  userId: string = 'Dirk';
  lobbyConnection: LobbyConnection;
  subscription: Subscription;

  constructor(private lobbyService: LobbyService) {
  }

  ngOnInit(): void {
  }

  connect() {
    this.lobbyConnection = this.lobbyService.connect(`ws://localhost:8080/ws/lobby`);

    if (!!this.subscription) {
      this.subscription.unsubscribe();
    }

    this.subscription = this.lobbyConnection.observable.subscribe(
      (msg) => {
        this.handleMessage(JSON.parse(msg.data))
      }
    )
  }

  handleMessage(msg: any) {
    this.messages.push(msg)
  }

  onSendClicked() {
    this.lobbyConnection.send({
      "_type": "Talk",
      message: this.msg
    });
  }

}
