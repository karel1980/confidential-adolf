import { Component, OnInit } from '@angular/core';
import {LobbyService} from "../lobby.service";

@Component({
  selector: 'app-lobby',
  templateUrl: './lobby.component.html',
  styleUrls: ['./lobby.component.scss']
})
export class LobbyComponent implements OnInit {

  messages = "foo\nbar\ngamma";

  msg:string;

  constructor(private lobbyService: LobbyService) { }

  ngOnInit(): void {
    this.lobbyService.connect('ws://localhost:8080/lobby/test1');

    this.lobbyService.observable.subscribe(
      (msg) => {
        console.log("Message received: ", msg);
      }
    )
  }

  onSendClicked() {
    console.log('I am a svc', this.lobbyService);
    console.log('I am a method', this.lobbyService.send);
    this.lobbyService.send(this.msg);
  }

}
