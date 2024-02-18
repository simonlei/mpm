export interface User {
  id?: number;
  account: string;
  isAdmin: boolean;
  name: string;
  passwd?: string;
  faceId?: number;
}
