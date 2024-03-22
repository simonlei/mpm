import {request} from "@/utils/request";
import {ActivityModel} from "@/api/model/activityModel";

const Api = {
  CreateOrUpdateActivity: '/createOrUpdateActivity',
  GetActivities: '/getActivities',
}

export function createOrUpdateActivity(activity: ActivityModel) {
  return request.post<number>({
    url: Api.CreateOrUpdateActivity, data: {
      activity: activity,
      fromPhoto: activity.fromPhoto,
    }
  });
}

export function getActivities() {
  return request.post<ActivityModel[]>({
    url: Api.GetActivities, data: {}
  });
}
