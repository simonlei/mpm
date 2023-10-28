export interface LoginUserInfoResult {
  data: Array<LoginUserInfo>;
}

export interface LoginUserInfo {
  ok: string;
  user: string;
  timestamp: string;
  signature: string;
}
