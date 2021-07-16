import 'package:app/conditions.dart';
import 'package:app/config.dart';
import 'package:app/event_bus.dart';
import 'package:app/select_model.dart';
import 'package:dio/dio.dart';
import 'package:filesize/filesize.dart';
import 'package:flutter/material.dart';
import 'package:logger/logger.dart';
import 'package:mutex/mutex.dart';

class PicsModel with ChangeNotifier {
  List<PicImage?> _pics = <PicImage>[];
  bool _init = false;
  bool metaDown = false;
  bool shiftDown = false;
  SelectModel _selectModel;

  PicsModel(this._selectModel) {
    loadImages(0, 75);
    BUS.on(EventBus.ConditionsChanged, (arg) async {
      _init = false;
      loadImages(0, 75);
    });
  }

  Future<List<PicImage?>> loadImages(int start, int size) async {
    Logger().i("load image from server...$start");
    var resp = await Dio().post(Config.api('/api/getPics'), data: Conditions.makeCondition(start, size));
    if (!_init) {
      _pics = List.filled(resp.data['totalRows'], null);
      BUS.emit(EventBus.CountChange, resp.data['totalRows']);
      _init = true;
    }
    List data = resp.data['data'];
    for (int i = 0; i < data.length; i++) {
      var element = data.elementAt(i);
      _pics[start + i] = PicImage(this, element);
    }
    notifyListeners();
    return _pics;
  }

  void select(int index) {
    _selectModel.select(metaDown, shiftDown, index);
  }

  void selectNext(int delta) {
    int nextIndex = _selectModel.lastIndex + delta;
    if (nextIndex > _pics.length - 1) nextIndex = _selectModel.lastIndex;
    if (nextIndex > _pics.length - 1) nextIndex = _pics.length - 1;
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

  Future<void> trashSelected() async {
    if (_selectModel.selected.isEmpty) return;

    var resp = await Dio()
        .post(Config.api("/api/trashPhotos"), data: _selectModel.selected.map((e) => _pics[e]!.name).toList());
    Logger().i("Result is $resp");
    if (resp.statusCode == 200 && resp.data == _selectModel.selected.length) {
      var list = _selectModel.selected.toList();
      list.sort();
      list.reversed.forEach((element) {
        _pics = List.from(_pics)..removeAt(element);
      });
      selectNext(0);
      BUS.emit(EventBus.CountChange, _pics.length);
      notifyListeners();
    } else {
      Logger().e("Result is $resp");
    }
  }
}

enum MediaType { photo, video }

class PicImage {
  PicImage(this._picsModel, element) {
    id = element['id'];
    width = element['width'];
    height = element['height'];
    thumb = element['thumb'];
    name = element['name'];
    size = element['size'];
    star = element['star'];
    description = element['description'];
    address = element['address'];
    takenDate = DateTime.parse(element['takendate']);
    mediaType = 'photo' == element['mediatype'] ? MediaType.photo : MediaType.video;
    duration = element['duration'] ?? 0.0;
  }

  String formatDuration() => Duration(seconds: duration.toInt()).toString().split('.').first.padLeft(8, "0");

  late final PicsModel _picsModel;
  late final int id;
  late final double width;
  late final double height;
  late String thumb;
  late final String name;
  late final int size;
  late String? description;
  late String? address;
  late DateTime takenDate;
  late bool star;
  late final MediaType mediaType;
  late final double duration;

  String getTooltip() {
    return "大小：${filesize(size)} \n" +
        "宽度：$width px\n" +
        "高度：$height px\n" +
        "描述：$description\n" +
        (address == null ? "" : "地址：$address\n") +
        "时间：${takenDate.year}-${takenDate.month.toString().padLeft(2, '0')}-${takenDate.day.toString().padLeft(2, '0')}";
  }

  Future<void> update(Map<String, dynamic> map) async {
    map.putIfAbsent('id', () => id);
    var resp = await Dio().post(Config.api("/api/updateImage"), data: map);
    if (resp.statusCode == 200) {
      //print('reseting...');
      resetElement(resp.data);
      _picsModel.notifyListeners();
    }
  }

  void resetElement(Map<String, dynamic> element) {
    //print(element);
    //print(element.containsKey('star'));
    star = element.containsKey('star') ? element['star'] : star;
    description = element.containsKey(['description'])
        ? element['description']
        : description == null
            ? ''
            : description;
    address = element.containsKey('address') ? element['address'] : address;
    takenDate = element.containsKey('takendate') ? DateTime.parse(element['takendate']) : takenDate;
    thumb = element.containsKey('thumb') ? element['thumb'] : thumb;
  }
}
