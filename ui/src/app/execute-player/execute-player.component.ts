import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild} from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Player} from "../lobby/lobby.reducer";

@Component({
  selector: 'app-execute-player',
  templateUrl: './execute-player.component.html',
  styleUrls: ['./execute-player.component.scss']
})
export class ExecutePlayerComponent implements OnInit, OnChanges {

  @Input() players: Player[]
  @Input() user: Player
  @Output() execute = new EventEmitter<Player>();
  @ViewChild('modal') modal;

  filteredPlayers: Player[];

  private modalRef;

  constructor(private modalService: NgbModal) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.players || !this.user) {
      this.filteredPlayers = this.players.filter(p => !p.dead)
        .filter(p => p.id != this.user.id);
    }
  }

  open() {
    if (!this.modalRef) {
      console.log('opening a modal', this.modal);
      this.modalRef = this.modalService.open(this.modal, {
        centered: true,
        backdrop: 'static',
        keyboard: false
      });
    }
  }

  onExecute(target: Player) {
    this.execute.emit(target);
    this.modalRef.close();
    this.modalRef = null;
  }

}
