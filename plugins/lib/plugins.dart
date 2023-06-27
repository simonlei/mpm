
import 'dart:typed_data';
import 'plugins_platform_interface.dart';

class Plugins {
  Future<String?> getPlatformVersion() {
    return PluginsPlatform.instance.getPlatformVersion();
  }
  Future<Uint8List?> heic2jpg(Uint8List data) {
    return PluginsPlatform.instance.heic2jpg(data);
  }
}
