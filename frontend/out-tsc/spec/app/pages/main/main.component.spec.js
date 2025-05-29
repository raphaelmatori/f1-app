import { TestBed } from '@angular/core/testing';
import { MainComponent } from './main.component';
import { F1Service } from '@app/shared/services/interfaces/f1.service.interface';
import { ErgastService } from '@app/shared/services/ergast.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
describe('MainComponent', () => {
    let component;
    let fixture;
    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [MainComponent, HttpClientTestingModule],
            providers: [
                { provide: F1Service, useClass: ErgastService }
            ]
        })
            .compileComponents();
    });
    beforeEach(() => {
        fixture = TestBed.createComponent(MainComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });
    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
