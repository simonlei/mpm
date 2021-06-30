import 'package:app/config.dart';
import 'package:app/event_bus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:logger/logger.dart';
import 'package:mutex/mutex.dart';

class PicsModel with ChangeNotifier {
  List<PicImage?> _pics = <PicImage>[];
  bool _init = false;
  bool ctrlDown = false;
  bool shiftDown = false;

  Future<List<PicImage?>> loadImages(int start, int size) async {
    // if (_pics != null) return _pics!;
    Logger().i("load image from server...$start");
    var resp = await Dio().post(Config.toUrl('/api/getPics'),
        data: {'start': start, 'size': size});
    // _pics.length(resp.data['totalRows']);
    if (!_init) {
      _pics = List.filled(resp.data['totalRows'], null);
      BUS.emit("countChange", resp.data['totalRows']);
      _init = true;
    }
    List data = resp.data['data'];
    for (int i = 0; i < data.length; i++) {
      var element = data.elementAt(i);
      _pics[start + i] = PicImage(element['width'], element['height'],
          element['thumb'], element['name']);
    }
    return _pics;
  }

  Set<int> _selectedSet = Set();

  void select(int index) {
    _selectedSet.forEach((element) {
      _pics[element]!.selected = false;
    });
    _selectedSet.clear();
    _pics[index]!.selected = true;
    _selectedSet.add(index);
    notifyListeners();
    /*
    set.forEach((element) {
      _pics[element]!.selected = true;
    });
    _selectedSet.addAll(set);

     */
  }

  bool isSelected(int index) {
    return _selectedSet.contains(index);
  }

  var _lock = Mutex();

  Future<PicImage?> getImage(int index) async {
    await _lock.acquire();
    try {
      if (!_init || _pics[index] == null) {
        Logger().i('image is null, load it');
        await loadImages(index, 75);
      }
      return _pics[index];
    } finally {
      _lock.release();
    }
  }

  int getTotalImages() {
    return _pics.length;
  }
}

class PicImage {
  PicImage(this.width, this.height, this.thumb, this.name);

  final double width;
  final double height;
  final String thumb;
  final String name;
  bool selected = false;
}
