import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LiberalLaneComponent } from './liberal-lane.component';

describe('LiberalLaneComponent', () => {
  let component: LiberalLaneComponent;
  let fixture: ComponentFixture<LiberalLaneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LiberalLaneComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LiberalLaneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
