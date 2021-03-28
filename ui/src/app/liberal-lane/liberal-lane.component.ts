import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-liberal-lane',
  templateUrl: './liberal-lane.component.html',
  styleUrls: ['./liberal-lane.component.scss']
})
export class LiberalLaneComponent implements OnInit {

  @Input() failedElections: number;
  @Input() enactedPolicies: number;

  tiles: number[] = [];

  constructor() { }

  ngOnInit(): void {
    this.tiles = [0, 1, 2, 3, 4]
  }

}
