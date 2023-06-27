// In order to *not* need this ignore, consider extracting the "web" version
// of your plugin as a separate package, instead of inlining it in the same
// package as the core of your plugin.
import 'dart:html' as html show window;
import 'dart:html';
import 'dart:typed_data';

import 'package:flutter_web_plugins/flutter_web_plugins.dart';
import 'package:js/js_util.dart';

import 'heic2any.dart';
import 'plugins_platform_interface.dart';

/// A web implementation of the PluginsPlatform of the Plugins plugin.
class PluginsWeb extends PluginsPlatform {
  /// Constructs a PluginsWeb
  PluginsWeb();

  static void registerWith(Registrar registrar) {
    PluginsPlatform.instance = PluginsWeb();
  }

  /// Returns a [String] containing the version of the platform.
  @override
  Future<String?> getPlatformVersion() async {
    final version = html.window.navigator.userAgent;
    return version;
  }

  @override
  Future<Uint8List?> heic2jpg(Uint8List data) {
    var result = heic2any(HeicParams(blob: Blob(data)));
    var future = promiseToFuture(result);
    // print('done');
    // print('$future : ${future.runtimeType}');
    future.then((value) {
      // print('Value is $value');
      var blob = value;
      // print('done2');
      // print('after heic2any $blob');
      return blob;
    }).catchError((error, stackTrace) {
      // print('error: $error ${error.runtimeType}');
      // print('error: $error ${error.toString()}');
      // print('stacktrace: $stackTrace');
    });
    throw Error();
  }
}
