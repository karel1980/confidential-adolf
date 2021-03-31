import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {Player} from "../lobby/lobby.reducer";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-player-picker',
  templateUrl: './player-picker.component.html',
  styleUrls: ['./player-picker.component.scss']
})
export class PlayerPickerComponent implements OnInit {

  @Input() players: Player[];
  @Output() playerClick = new EventEmitter<Player>();
  @ViewChild("content") content;

  question: string;

  modalRef: any;

  constructor(private modalService: NgbModal) { }

  ngOnInit(): void {
  }

  open(question: string) {
    if (!this.modalRef) {
      this.question = question;
      this.modalRef = this.modalService.open(this.content, {
        centered: true,
        backdrop: 'static',
        keyboard: false
      })
    }
  }

  onPlayerClick(player: Player) {
    this.playerClick.emit(player);
    this.modalRef.close();
    this.modalRef = null;
  }
}
