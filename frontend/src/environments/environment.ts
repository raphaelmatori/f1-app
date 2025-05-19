export const environment = {
  production: false,
  apiF1Series: "https://ergast.com/api/f1",
  endpoints: {
    seasons: "seasons.json",
    allRacesOfAYear: (year: number) => `${year}/races.json`,
    worldChampionByYear: (year: number) => `${year}/driverStandings/1.json`,
    allRacesWinnersOfAYear: (year: number) => `${year}/results/1.json`,
  },
  config: {
    pagination: {
      pageLimit: 30,
    },
    initialYearForF1Series: 1950,
  },
};
