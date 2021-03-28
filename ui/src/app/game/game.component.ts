import {Component, OnInit, Output, EventEmitter, Input} from '@angular/core';
import {Game, GameTO, PolicyTile, Vote} from "../lobby/lobby.reducer";
@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit {

  @Output() pingClicked = new EventEmitter();
  @Output() getRoomStateClicked = new EventEmitter();
  @Output() startGameClicked = new EventEmitter();
  @Output() voteLeadership = new EventEmitter<Vote>();
  @Output() discardPolicyTile = new EventEmitter<PolicyTile>();
  @Output() requestVeto = new EventEmitter();
  @Output() confirmVeto = new EventEmitter();
  @Output() denyVeto = new EventEmitter();
  @Output() policyPeek = new EventEmitter();
  @Output() nominateChancellor = new EventEmitter<string>();
  @Output() investigateLoyalty = new EventEmitter<string>();
  @Output() callSpecialElection = new EventEmitter<string>();
  @Output() execution = new EventEmitter<string>();

  @Input() game: Game;

  constructor() { }

  ngOnInit(): void {
  }

}
