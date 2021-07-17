class Conditions {
  static bool trashed = false;
  static String order = '-id';
  static String dateKey = '';
  static String path = '';

  static makeCondition(int start, int size) {
    return {
      'trashed': trashed,
      'start': start,
      'size': size,
      'order': order,
      'dateKey': dateKey,
      'path': path,
    };
  }
}