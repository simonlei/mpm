class Config {
  static String getApiUrl() {
    //if ('true' == Platform.environment['isDev'])
    return 'http://127.0.0.1:8080';
    //return '';
  }

  static String toUrl(String api) {
    return getApiUrl() + api;
  }

  static String imageBase = '';

  static imageUrl(String thumb) {
    return imageBase + thumb;
  }

  static void setImageBase(base) {
    imageBase = base;
  }
}