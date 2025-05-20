export interface Paginate {
  MRData: {
    xmlns: string;
    series: string;
    url: string;
    limit: string;
    offset: string;
    total: string;
    RaceTable?: {
      season: string;
      Races: any[];
    };
    DriverTable?: {
      season: string;
      Drivers: any[];
    };
    ConstructorTable?: {
      season: string;
      Constructors: any[];
    };
  };
  results: any[];
} 