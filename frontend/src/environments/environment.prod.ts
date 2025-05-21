export const environment = {
  production: true,
  apiF1Series: "http://localhost:8080/api/v1",
  endpoints: {
    seasons: "seasons.json",
    allRacesWinnersOfAYear: (year: number) => `races/${year}`,
    worldChampionByYear: (year: number) => `champions/${year}`,
  },
  config: {
    pagination: {
      pageLimit: 30,
    },
    initialYearForF1Series: 1950,
  },
};
