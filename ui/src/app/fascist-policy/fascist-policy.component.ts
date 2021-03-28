import {Component, Input, OnInit} from '@angular/core';
import {ExecutivePower} from "../lobby/lobby.reducer";

@Component({
  selector: 'app-fascist-policy',
  templateUrl: './fascist-policy.component.html',
  styleUrls: ['./fascist-policy.component.scss']
})
export class FascistPolicyComponent implements OnInit {

  @Input() type: ExecutivePower;
  @Input() enabled: boolean;

  constructor() { }

  ngOnInit(): void {
  }

}
