import {FaceInfo} from "@/api/model/photos";
import {updateFace} from "@/api/photos";
import {MessagePlugin} from "tdesign-vue-next";
import {dialogsStore} from "@/store";

const dlgStore = dialogsStore();

export function changeFaceName(face: FaceInfo) {
  dlgStore.textInputTitle = '请输入对应的人名';
  dlgStore.textInputValue = face.name;
  dlgStore.textInputDlg = true;

  function realChangeName() {
    return (inputValue: string) => {
      updateFace({faceId: face.faceId, name: inputValue}).then((result: Boolean) => {
        if (result) {
          face.name = inputValue;
        } else {
          MessagePlugin.error("有重名的人存在，请更换名字再试");
          dlgStore.textInputDlg = true;
          dlgStore.whenInputConfirmed(realChangeName());
        }
      });
    };
  }

  dlgStore.whenInputConfirmed(realChangeName());
}
