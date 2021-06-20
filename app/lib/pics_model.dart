import 'package:app/config.dart';
import 'package:dio/dio.dart';

class PicsModel {
  late List<PicImage> _pics;

  Future<List<PicImage>> loadImages() async {
    var resp = await Dio().post(Config.toUrl('/api/getPics'));
    PicImage emptyOne = PicImage(200, 150, '');
    List<PicImage> images = List.filled(resp.data['totalRows'], emptyOne);
    List data = resp.data['data'];
    for (int i = 0; i < data.length; i++) {
      var element = data.elementAt(i);
      images[i] =
          PicImage(element['width'], element['height'], element['thumb']);
    }
    _pics = images;
    return images;
  }

  Set<int> _selectedSet = Set();

  void select(Set<int> set) {
    _selectedSet.clear();
    _selectedSet.addAll(set);
  }

  bool isSelected(int index) {
    return _selectedSet.contains(index);
  }

  PicImage getImage(int index) {
    return _pics[index];
  }
}

class PicImage {
  const PicImage(this.width, this.height, this.thumb);

  final double width;
  final double height;
  final String thumb;
}
