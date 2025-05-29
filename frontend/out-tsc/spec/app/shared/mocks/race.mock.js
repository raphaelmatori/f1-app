export const mockDriver = {
    driverId: 'max_verstappen',
    code: 'VER',
    givenName: 'Max',
    familyName: 'Verstappen',
    nationality: 'Dutch',
    url: 'http://en.wikipedia.org/wiki/Max_Verstappen',
    dateOfBirth: '1997-09-30'
};
export const mockConstructor = {
    constructorId: 'red_bull',
    name: 'Red Bull Racing',
    nationality: 'Austrian',
    url: 'http://en.wikipedia.org/wiki/Red_Bull_Racing'
};
export const mockCircuit = {
    circuitId: 'albert_park',
    circuitName: 'Albert Park Grand Prix Circuit',
    locality: 'Melbourne',
    country: 'Australia'
};
export const mockResult = {
    position: '1',
    points: '25',
    grid: '1',
    laps: '58',
    status: 'Finished',
    driver: mockDriver,
    constructor: mockConstructor,
    time: {
        millis: '5259892',
        time: '1:27:39.892'
    }
};
export const mockRace = {
    season: 2024,
    round: 1,
    raceName: 'Australian Grand Prix',
    date: '2024-03-24',
    time: '05:00:00Z',
    circuit: mockCircuit,
    results: [mockResult],
    winner: mockDriver,
    constructor: mockConstructor
};
