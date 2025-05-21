export const environment = {
  production: false,
  apiF1Series: "http://localhost:8080/api/v1",
  endpoints: {
    allRacesWinnersOfAYear: (year: number) => `races/${year}`,
    worldChampions: `champions`,
  },
  config: {
    pagination: {
      pageLimit: 30,
    },
    initialYearForF1Series: 1950,
  },
};
