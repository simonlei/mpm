import 'dart:math';

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SelectModel with ChangeNotifier {
  Set<int> _selectedSet = Set();
  int lastIndex = -1;

  Set<int> get selected => Set.unmodifiable(_selectedSet);

  Future<void> select(bool metaDown, bool shiftDown, int index) async {
    if (!metaDown && !shiftDown) {}
    if (shiftDown && lastIndex > 0) {
      for (int i = min(lastIndex, index); i <= max(lastIndex, index); i++) {
        _selectedSet.add(i);
      }
      lastIndex = index;
    } else if (metaDown) {
      lastIndex = index;
      if (_selectedSet.contains(index))
        _selectedSet.remove(index);
      else
        _selectedSet.add(index);
    } else {
      _selectedSet.clear();
      lastIndex = index;
      _selectedSet.add(index);
    }
    SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setInt("lastIndex", index);
    notifyListeners();
  }

  bool isSelected(int index) {
    return _selectedSet.contains(index);
  }

  void clearSelect() {
    lastIndex = -1;
    _selectedSet.clear();
  }
}
