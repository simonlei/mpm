import 'package:app/config.dart';
import 'package:app/event_bus.dart';
import 'package:app/select_model.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:logger/logger.dart';
import 'package:mutex/mutex.dart';

class PicsModel with ChangeNotifier {
  List<PicImage?> _pics = <PicImage>[];
  bool _init = false;
  bool metaDown = false;
  bool shiftDown = false;
  SelectModel _selectModel;

  PicsModel(this._selectModel);

  Future<List<PicImage?>> loadImages(int start, int size) async {
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

  void select(int index) {
    _selectModel.select(metaDown, shiftDown, index);
  }

  void selectNext(int delta) {
    int nextIndex = _selectModel.lastIndex + delta;
    if (nextIndex > _pics.length - 1) nextIndex = _selectModel.lastIndex;
    if (nextIndex < 0) nextIndex = _selectModel.lastIndex;

    _selectModel.select(metaDown, shiftDown, nextIndex);
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

  int getSelectedIndex() {
    return _selectModel.lastIndex;
  }
}

class PicImage {
  PicImage(this.width, this.height, this.thumb, this.name);

  final double width;
  final double height;
  final String thumb;
  final String name;
}
