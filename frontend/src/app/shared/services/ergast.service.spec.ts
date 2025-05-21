import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ErgastService } from './ergast.service';
import { environment } from 'environments/environment';
import { Driver } from '@app/models/driver.interface';
import {Champion} from "@app/models/champion.interface";

// Mock environment endpoints if needed

describe('ErgastService', () => {
  let service: ErgastService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ErgastService]
    });
    service = TestBed.inject(ErgastService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch world champions', () => {
    const mockChampions = [
      { year: 2023, driverId: 'test-driver-1', givenName: 'Test', familyName: 'Driver 1' },
      { year: 2022, driverId: 'test-driver-2', givenName: 'Test', familyName: 'Driver 2' }
    ];

    service.getWorldChampions().subscribe(champions => {
      expect(champions).toBeTruthy();
      expect(champions instanceof Map).toBeTrue();
      expect(champions?.get(2023)?.driverId).toBe('test-driver-1');
      expect(champions?.get(2022)?.driverId).toBe('test-driver-2');
    });

    const req = httpMock.expectOne(`${environment.apiF1Series}/${environment.endpoints.worldChampions}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockChampions);
  });
}); 