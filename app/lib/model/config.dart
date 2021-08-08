import 'dart:core';

class Config {
  static const url = String.fromEnvironment('remote_addr', defaultValue: 'http://127.0.0.1:8080');

  static String api(String api) {
    if (url.isEmpty) {
      return 'http://${Uri.base.host}:${Uri.base.port}$api';
    }
    return url + api;
  }

  static String imageBase = '';

  static imageUrl(String name) {
    return imageBase + name;
  }

  static void setImageBase(base) {
    imageBase = base;
  }

  static String region = '';
  static String bucket = '';
}