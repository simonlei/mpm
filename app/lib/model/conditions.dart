class Conditions {
  static bool trashed = false;
  static bool? star;
  static bool? video;
  static String order = '-takenDate';
  static String dateKey = '';
  static String path = '';
  static String? tag;

  static makeCondition(int start, int size) {
    return {
      'trashed': trashed,
      'start': start,
      'size': size,
      'order': order,
      'dateKey': dateKey,
      'path': path,
      'star': star,
      'video': video,
      'tag': tag,
    };
  }
}
