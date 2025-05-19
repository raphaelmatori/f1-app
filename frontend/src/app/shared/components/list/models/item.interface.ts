export interface Item {
  id: string;
  selected?: boolean;
  title: string;
  additionalInfo?: { [key: string]: string };
}
