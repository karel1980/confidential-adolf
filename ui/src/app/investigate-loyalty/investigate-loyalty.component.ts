import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {Player} from "../lobby/lobby.reducer";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-investigate-loyalty',
  templateUrl: './investigate-loyalty.component.html',
  styleUrls: ['./investigate-loyalty.component.scss']
})
export class InvestigateLoyaltyComponent implements OnInit {

  @Input() players: Player[];
  @Output() investigate = new EventEmitter<Player>();
  @ViewChild('modal') modal;

  private modalRef;

  constructor(private modalService: NgbModal) {
  }

  ngOnInit(): void {
  }

  open() {
    if (!this.modalRef) {
      this.modalRef = this.modalService.open(this.modal);
    }
  }

  onInvestigatePlayer(player: Player) {
    this.investigate.emit(player);
    this.modalRef.close();
    this.modalRef = null;
  }
}
