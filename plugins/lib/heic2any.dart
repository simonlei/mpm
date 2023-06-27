@JS()
library heic2any.js;

import 'dart:html';

import 'package:js/js.dart';

@JS('heic2any')
external Object heic2any(HeicParams heicParams);

@JS()
@anonymous
class HeicParams {
  external Blob blob;

  external factory HeicParams({Blob blob});
}
