export const environment = {
    production: false,
    apiF1Series: "http://localhost:8080/api/v1",
    endpoints: {
        allRacesWinnersOfAYear: (year) => `races/${year}`,
        worldChampions: `champions`,
    }
};
