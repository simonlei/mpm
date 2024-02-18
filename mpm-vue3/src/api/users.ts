import {request} from "@/utils/request";
import {LoginUserInfo} from "@/api/model/loginUserInfo";
import {User} from "@/api/model/users";

const Api = {
  CheckPassword: '/checkPassword',
  AuthPassword: '/authPassword',
  CreateOrUpdateUser: '/createOrUpdateUser',
  LoadUsers: '/loadUsers',
  LoadUser: '/loadUser',
  DeleteUser: '/deleteUser',
};

export function loadUser(id: number) {
  return request.post<User>({url: Api.LoadUser, data: {id: id}});
}

export function deleteUser(id: number) {
  return request.post<Boolean>({url: Api.DeleteUser, data: {id: id}});
}

export function loadUsers() {
  return request.post<User[]>({url: Api.LoadUsers,});
}

export function checkPassword(account: string, passwd: string) {
  return request.post<User>({url: Api.CheckPassword, data: {account: account, passwd: passwd}});
}

export function createOrUpdateUser(user: User) {
  return request.post<Boolean>({url: Api.CreateOrUpdateUser, data: user});
}

export function getLoginUserInfo(userInfo: Record<string, unknown>) {
  return request.post<LoginUserInfo>({
    url: Api.AuthPassword,
    data: userInfo,
  });
}
