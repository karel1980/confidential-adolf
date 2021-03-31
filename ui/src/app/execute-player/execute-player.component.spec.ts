import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExecutePlayerComponent } from './execute-player.component';

describe('ExecutePlayerComponent', () => {
  let component: ExecutePlayerComponent;
  let fixture: ComponentFixture<ExecutePlayerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExecutePlayerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExecutePlayerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
