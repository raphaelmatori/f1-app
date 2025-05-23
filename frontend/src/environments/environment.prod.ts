export const environment = {
  production: true,
  enableDebug: false,
  cacheTimeout: 3600, // 1 hour in seconds
  maxRetries: 3,
  retryDelay: 1000, // 1 second in milliseconds
  apiF1Series: "https://f1-app-272673308780.europe-west4.run.app/api/v1",
  endpoints: {
    seasons: "seasons.json",
    allRacesWinnersOfAYear: (year: number) => `races/${year}`,
    worldChampions: `champions`,
  }
};
