import 'package:app/config.dart';
import 'package:app/image_tile.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';

class PicsGrid extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _PicsGridState();
  }
}

class _PicsGridState extends State<PicsGrid> {
//  _PicsGridState() : _images = _loadImages();
//  final List<PicImage> _images;

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<List<PicImage>>(
      future: _loadImages(),
      builder: (BuildContext context, AsyncSnapshot<List<PicImage>> snapshot) {
        print('Has data:');
        print(snapshot.data);
        print(snapshot.error);
        if (snapshot.hasData) {
          List<PicImage> data = snapshot.data!;
          return StaggeredGridView.extentBuilder(
            maxCrossAxisExtent: 200,
            crossAxisSpacing: 3,
            mainAxisSpacing: 3,
            itemCount: data.length,
            itemBuilder: (context, index) =>
                ImageTile(index, data.elementAt(index)),
            staggeredTileBuilder: (index) => const StaggeredTile.extent(1, 150),
          );
        } else if (snapshot.hasError)
          return Text('Error:${snapshot.error}');
        else
          return CircularProgressIndicator();
      },
    );
  }
}

Future<List<PicImage>> _loadImages() async {
  var resp = await Dio().post(Config.toUrl('/api/getPics'));
  PicImage emptyOne = PicImage(200, 150, '');
  List<PicImage> images = List.filled(resp.data['totalRows'], emptyOne);
  List data = resp.data['data'];
  for (int i = 0; i < data.length; i++) {
    var element = data.elementAt(i);
    images[i] = PicImage(element['width'], element['height'], element['thumb']);
  }

  return images;
}
