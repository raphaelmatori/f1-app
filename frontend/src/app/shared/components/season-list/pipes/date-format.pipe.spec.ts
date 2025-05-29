import { DateFormatPipe } from './date-format.pipe';

describe('DateFormatPipe', () => {
  let pipe: DateFormatPipe;

  beforeEach(() => {
    pipe = new DateFormatPipe();
  });

  it('should create', () => {
    expect(pipe).toBeTruthy();
  });

  it('should format date correctly', () => {
    const date = '2024-03-24';
    const result = pipe.transform(date);
    expect(result).toEqual('Mar 24');
  });

  it('should handle different months', () => {
    const dates = [
      { input: '2024-01-01', expected: 'Jan 1' },
      { input: '2024-05-15', expected: 'May 15' },
      { input: '2024-12-31', expected: 'Dec 31' }
    ];

    dates.forEach(({ input, expected }) => {
      expect(pipe.transform(input)).toEqual(expected);
    });
  });

  it('should handle single digit days', () => {
    const date = '2024-03-05';
    const result = pipe.transform(date);
    expect(result).toEqual('Mar 5');
  });

  it('should handle invalid date input', () => {
    const invalidDate = 'invalid-date';
    const result = pipe.transform(invalidDate);
    expect(result).toEqual('Invalid Date');
  });

  it('should handle empty string input', () => {
    const emptyDate = '';
    const result = pipe.transform(emptyDate);
    expect(result).toEqual('Invalid Date');
  });

  it('should handle null input', () => {
    const result = pipe.transform(null as any);
    expect(result).toEqual('Invalid Date');
  });

  it('should handle undefined input', () => {
    const result = pipe.transform(undefined as any);
    expect(result).toEqual('Invalid Date');
  });

  it('should handle date with time component', () => {
    const dateWithTime = '2024-03-24T14:30:00Z';
    const result = pipe.transform(dateWithTime);
    expect(result).toEqual('Mar 24');
  });

  it('should handle leap year dates', () => {
    const leapYearDate = '2024-02-29';
    const result = pipe.transform(leapYearDate);
    expect(result).toEqual('Feb 29');
  });

  it('should handle dates from different years', () => {
    const dates = [
      { input: '2023-12-31', expected: 'Dec 31' },
      { input: '2024-01-01', expected: 'Jan 1' },
      { input: '2025-06-15', expected: 'Jun 15' }
    ];

    dates.forEach(({ input, expected }) => {
      expect(pipe.transform(input)).toEqual(expected);
    });
  });
}); 