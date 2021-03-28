import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ElectionTrackerComponent } from './election-tracker.component';

describe('ElectionTrackerComponent', () => {
  let component: ElectionTrackerComponent;
  let fixture: ComponentFixture<ElectionTrackerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ElectionTrackerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ElectionTrackerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
