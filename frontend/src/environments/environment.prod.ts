export const environment = {
  production: true,
  enableDebug: false,
  cacheTimeout: 3600, // 1 hour in seconds
  maxRetries: 3,
  retryDelay: 1000, // 1 second in milliseconds
  apiF1Series: "https://api.f1-champions-explorer.com/api/v1",
  endpoints: {
    seasons: "seasons.json",
    allRacesWinnersOfAYear: (year: number) => `races/${year}`,
    worldChampions: `champions`,
  },
  config: {
    pagination: {
      pageLimit: 30,
    },
    initialYearForF1Series: 1950,
  },
  monitoring: {
    enableErrorTracking: true,
    enablePerformanceMonitoring: true,
    logLevel: 'error',
  },
  security: {
    enableCSP: true,
    enableHSTS: true,
    enableXSSProtection: true,
  }
};
