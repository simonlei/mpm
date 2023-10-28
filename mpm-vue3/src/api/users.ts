import {request} from "@/utils/request";
import {LoginUserInfo} from "@/api/model/loginUserInfo";

const Api = {
  AuthPassword: '/authPassword',
};

export function getLoginUserInfo(userInfo: Record<string, unknown>) {
  return request.post<LoginUserInfo>({
    url: Api.AuthPassword,
    data: userInfo,
  });
}
