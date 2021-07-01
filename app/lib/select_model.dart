import 'dart:math';

import 'package:flutter/material.dart';

class SelectModel with ChangeNotifier {
  Set<int> _selectedSet = Set();
  int lastIndex = -1;

  void select(bool metaDown, bool shiftDown, int index) {
    if (!metaDown && !shiftDown) {}
    if (shiftDown && lastIndex > 0) {
      for (int i = min(lastIndex, index); i <= max(lastIndex, index); i++) {
        _selectedSet.add(i);
      }
      lastIndex = index;
    } else if (metaDown) {
      lastIndex = index;
      _selectedSet.add(index);
    } else {
      _selectedSet.clear();
      lastIndex = index;
      _selectedSet.add(index);
    }
    notifyListeners();
  }

  bool isSelected(int index) {
    return _selectedSet.contains(index);
  }

  void selectNext(bool metaDown, bool shiftDown, int delta) {}
}
