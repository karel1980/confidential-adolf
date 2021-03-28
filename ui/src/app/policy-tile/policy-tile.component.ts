import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-policy-tile',
  templateUrl: './policy-tile.component.html',
  styleUrls: ['./policy-tile.component.scss']
})
export class PolicyTileComponent implements OnInit {

  @Input() faction: string;
  @Input() type: string;

  constructor() { }

  ngOnInit(): void {
  }

}
