import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-election-tracker',
  templateUrl: './election-tracker.component.html',
  styleUrls: ['./election-tracker.component.scss']
})
export class ElectionTrackerComponent implements OnInit {

  @Input() failedElections: number;

  constructor() { }

  ngOnInit(): void {
  }

}
