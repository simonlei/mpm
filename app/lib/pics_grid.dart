import 'package:flutter/cupertino.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';

class PicsGrid extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _PicsGridState();
  }
}

class _PicsGridState extends State<PicsGrid> {
  _PicsGridState() : _images = _loadImages();
  final List<PicImage> _images;

  @override
  Widget build(BuildContext context) {
    return StaggeredGridView.countBuilder(
        crossAxisCount: 4,
        itemCount: _images.length,
        itemBuilder: (context, index) => _ImageTile(index, _images[index]),
        staggeredTileBuilder: (index) => const StaggeredTile.fit(2));
  }
}

class _ImageTile extends StatelessWidget {
  const _ImageTile(this.index, this.image);

  final int index;
  final PicImage image;

  @override
  Widget build(BuildContext context) {
    return Text('yes');
  }
}

class PicImage {
  const PicImage(this.width, this.height);

  final int width;
  final int height;
}

List<PicImage> _loadImages() {
  return List.empty();
}
