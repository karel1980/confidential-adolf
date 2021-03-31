import {Component, Input, OnInit, Output, EventEmitter} from '@angular/core';
import {PolicyTile} from '../lobby/lobby.reducer';

@Component({
  selector: 'app-policy-tile',
  templateUrl: './policy-tile.component.html',
  styleUrls: ['./policy-tile.component.scss']
})
export class PolicyTileComponent implements OnInit {

  @Input() faction: PolicyTile;
  @Input() type: string;
  @Input() enacted: boolean;
  @Input() clickable: boolean;
  @Output() tileClick = new EventEmitter<PolicyTile>();

  constructor() { }

  ngOnInit(): void {
  }

}
