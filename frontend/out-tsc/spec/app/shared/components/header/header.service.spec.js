import { TestBed } from '@angular/core/testing';
import { HeaderService } from './header.service';
describe('HeaderService', () => {
    let service;
    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [HeaderService]
        });
        service = TestBed.inject(HeaderService);
    });
    it('should be created', () => {
        expect(service).toBeTruthy();
    });
    it('should set and get title', () => {
        service.setTitle('Test Title');
        expect(service.getTitle()).toBe('Test Title');
    });
});
