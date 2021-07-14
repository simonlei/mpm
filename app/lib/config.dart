class Config {
  static String getApiUrl() {
    //if ('true' == Platform.environment['isDev'])
    return 'http://127.0.0.1:8080';
    //return '';
  }

  static String api(String api) {
    return getApiUrl() + api;
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
