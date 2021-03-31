import {Component, Input, OnInit} from '@angular/core';
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

  constructor() { }

  ngOnInit(): void {
  }

}
