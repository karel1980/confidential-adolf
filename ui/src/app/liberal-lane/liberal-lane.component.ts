import {Component, Input, OnInit} from '@angular/core';
import {PolicyTile} from "../lobby/lobby.reducer";

@Component({
  selector: 'app-liberal-lane',
  templateUrl: './liberal-lane.component.html',
  styleUrls: ['./liberal-lane.component.scss']
})
export class LiberalLaneComponent implements OnInit {

  @Input() failedElections: number;
  @Input() enactedPolicies: number;

  PolicyTile = PolicyTile;

  tiles: number[] = [];

  constructor() { }

  ngOnInit(): void {
    this.tiles = [0, 1, 2, 3, 4]
  }

}
