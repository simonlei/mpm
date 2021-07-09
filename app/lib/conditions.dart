class Conditions {
  static bool trashed = false;

  static makeCondition(int start, int size) {
    return {
      'trashed': trashed,
      'start': start,
      'size': size,
    };
  }
}
