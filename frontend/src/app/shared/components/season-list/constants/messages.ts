export const MESSAGES = {
  LOADING: {
    SEASONS: 'Loading seasons...',
    RACE_DATA: (year: number) => `Loading race data for ${year}`
  },
  ERROR: {
    LOAD_CHAMPIONS: 'Error loading F1 World Champions',
    NO_RACE_DATA: (year: number) => `No race data available for ${year}`
  },
  LABELS: {
    SEASON_IN_PROGRESS: 'SEASON IN PROGRESS',
    ONGOING: 'ONGOING',
    CHAMPIONSHIP_ONGOING: 'Championship Ongoing',
    CHAMPION_TO_BE_DETERMINED: 'Champion to be determined',
    VIEW_RACE_WINNERS: 'View Race Winners',
    TOGGLE_RACE_LIST: (year: number) => `Toggle race list for ${year} season`
  }
}; 