import 'package:app/image_tile.dart';
import 'package:app/pics_model.dart';
import 'package:flutter/material.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';

class PicsGrid extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _PicsGridState();
  }
}

class _PicsGridState extends State<PicsGrid> {
  PicsModel _picsModel = PicsModel();

  void _selectImage(Set<int> index) {
    setState(() {
      _picsModel.select(index);
    });
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<PicImage?>(
      future: _picsModel.getImage(0),
      builder: (BuildContext context, AsyncSnapshot<PicImage?> snapshot) {
        if (snapshot.hasData) {
          return StaggeredGridView.extentBuilder(
            maxCrossAxisExtent: 200,
            crossAxisSpacing: 5,
            mainAxisSpacing: 3,
            itemCount: _picsModel.getTotalImages(),
            itemBuilder: (context, index) =>
                ImageTile(index, _picsModel, _selectImage),
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
