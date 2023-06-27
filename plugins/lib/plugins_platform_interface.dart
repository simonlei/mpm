import 'dart:typed_data';

import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'plugins_method_channel.dart';

abstract class PluginsPlatform extends PlatformInterface {
  /// Constructs a PluginsPlatform.
  PluginsPlatform() : super(token: _token);

  static final Object _token = Object();

  static PluginsPlatform _instance = MethodChannelPlugins();

  /// The default instance of [PluginsPlatform] to use.
  ///
  /// Defaults to [MethodChannelPlugins].
  static PluginsPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [PluginsPlatform] when
  /// they register themselves.
  static set instance(PluginsPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<Uint8List?> heic2jpg(Uint8List data) {
    throw UnimplementedError('heic2jpg() has not been implemented.');
  }
}
