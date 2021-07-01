import 'dart:math';

import 'package:flutter/material.dart';

class SelectModel with ChangeNotifier {
  Set<int> _selectedSet = Set();
  int _lastIndex = -1;

  void select(bool metaDown, bool shiftDown, int index) {
    if (!metaDown && !shiftDown) {}
    if (shiftDown && _lastIndex > 0) {
      for (int i = min(_lastIndex, index); i <= max(_lastIndex, index); i++) {
        _selectedSet.add(i);
      }
      _lastIndex = index;
    } else if (metaDown) {
      _lastIndex = index;
      _selectedSet.add(index);
    } else {
      _selectedSet.clear();
      _lastIndex = index;
      _selectedSet.add(index);
    }
    notifyListeners();
  }

  bool isSelected(int index) {
    return _selectedSet.contains(index);
  }
}
