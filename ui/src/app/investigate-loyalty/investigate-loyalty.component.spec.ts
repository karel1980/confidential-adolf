import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvestigateLoyaltyComponent } from './investigate-loyalty.component';

describe('InvestigateLoyaltyComponent', () => {
  let component: InvestigateLoyaltyComponent;
  let fixture: ComponentFixture<InvestigateLoyaltyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InvestigateLoyaltyComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InvestigateLoyaltyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
