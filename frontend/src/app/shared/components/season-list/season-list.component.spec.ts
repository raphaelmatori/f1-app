import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SeasonListComponent } from './season-list.component';
import { ErgastService } from "@app/shared/services/ergast.service";
import { F1Service } from "@app/shared/services/interfaces/f1.service.interface";
import { provideHttpClientTesting } from "@angular/common/http/testing";


describe('SeasonListComponent', () => {
  let component: SeasonListComponent;
  let fixture: ComponentFixture<SeasonListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeasonListComponent],
      providers: [
        provideHttpClientTesting(),
        {
          provide: F1Service,
          useClass: ErgastService
        }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(SeasonListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // Add more tests here as needed for any logic in SeasonListComponent
}); 