@JS()
library cos.js;

import 'package:js/js.dart';

@JS()
class COS {
  // external COS({getAuthorization: Function});
/*
  external factory COS(
      {void Function(dynamic options, dynamic callback) getAuthorization});
*/
  external COS(dynamic options);

  external void uploadFiles(dynamic params, Function callback);
}
