import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:plugins/plugins.dart';
import 'package:plugins/plugins_method_channel.dart';
import 'package:plugins/plugins_platform_interface.dart';

class MockPluginsPlatform
    with MockPlatformInterfaceMixin
    implements PluginsPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<Uint8List?> heic2jpg(blob) => Future.value(blob);
}

void main() {
  final PluginsPlatform initialPlatform = PluginsPlatform.instance;

  test('$MethodChannelPlugins is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelPlugins>());
  });

  test('getPlatformVersion', () async {
    Plugins pluginsPlugin = Plugins();
    MockPluginsPlatform fakePlatform = MockPluginsPlatform();
    PluginsPlatform.instance = fakePlatform;

    expect(await pluginsPlugin.getPlatformVersion(), '42');
  });
}
