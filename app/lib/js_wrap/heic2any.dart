@JS()
library heic2any.js;

import 'dart:html';

import 'package:js/js.dart';

@JS('heic2any')
external Blob heic2any(Blob blob);
