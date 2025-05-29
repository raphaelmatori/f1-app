export const environment = {
  production: false,
  apiF1Series: "https://f1-app-272673308780.europe-west4.run.app/api/v1",
  endpoints: {
    allRacesWinnersOfAYear: (year: number) => `races/${year}`,
    worldChampions: `champions`,
  }
};
