@JS()
library cos.js;

import 'dart:html';

import 'package:js/js.dart';

@JS()
class COS {
  // external COS({getAuthorization: Function});
/*
  external factory COS(
      {void Function(dynamic options, dynamic callback) getAuthorization});
*/
  external COS(CosInitParam params);

  external void uploadFiles(UploadFilesParams params, Function callback);
}

@JS()
@anonymous
class CosInitParam {
  external Function get onProgress;

  external factory CosInitParam({Function getAuthorization});
}

@JS()
@anonymous
class AuthData {
  external String get TmpSecretId;

  external String get TmpSecretKey;

  external String get XCosSecurityToken;

  external int get StartTime;

  external int get ExpiredTime;

  external factory AuthData(
      {String TmpSecretId,
      String TmpSecretKey,
      String XCosSecurityToken,
      int StartTime,
      int ExpiredTime});
}

@JS()
@anonymous
class UploadFilesParams {
  external List<CosFile> get files;

  external Function get onProgress;

  external Function get onFileFinish;

  external factory UploadFilesParams(
      {List<CosFile> files, Function onProgress, Function onFileFinish});
}

@JS()
@anonymous
class CosFile {
  external String get Key;

  external String get Bucket;

  external String get Region;

  external File get Body;

  external factory CosFile(
      {String Region, String Bucket, String Key, File Body});
}
