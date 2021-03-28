import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FascistPolicyComponent } from './fascist-policy.component';

describe('FascistPolicyComponent', () => {
  let component: FascistPolicyComponent;
  let fixture: ComponentFixture<FascistPolicyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FascistPolicyComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FascistPolicyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
