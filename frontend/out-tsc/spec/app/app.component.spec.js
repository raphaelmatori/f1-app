import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { RouterTestingModule } from '@angular/router/testing';
import { F1Service } from '@app/shared/services/interfaces/f1.service.interface';
import { ErgastService } from '@app/shared/services/ergast.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
describe('AppComponent', () => {
    let component;
    let fixture;
    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                AppComponent,
                RouterTestingModule,
                HttpClientTestingModule
            ],
            providers: [
                { provide: F1Service, useClass: ErgastService }
            ]
        })
            .compileComponents();
    });
    beforeEach(() => {
        fixture = TestBed.createComponent(AppComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });
    it('should create', () => {
        expect(component).toBeTruthy();
    });
    it('should have title f1-app', () => {
        expect(component.title).toBe('f1-app');
    });
});
