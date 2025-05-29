import { TestBed } from '@angular/core/testing';
import { SpinnerComponent } from './spinner.component';
import { HttpClientTestingModule } from "@angular/common/http/testing";
describe('SpinnerComponent', () => {
    let component;
    let fixture;
    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SpinnerComponent, HttpClientTestingModule]
        }).compileComponents();
        fixture = TestBed.createComponent(SpinnerComponent);
        component = fixture.componentInstance;
    });
    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
