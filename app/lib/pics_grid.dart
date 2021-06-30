import 'package:app/image_tile.dart';
import 'package:app/pics_model.dart';
import 'package:flutter/material.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';
import 'package:logger/logger.dart';
import 'package:provider/provider.dart';

class PicsGrid extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _PicsGridState();
  }
}

class _PicsGridState extends State<PicsGrid> {
  @override
  Widget build(BuildContext context) {
    PicsModel _picsModel = Provider.of<PicsModel>(context, listen: false);

    return Focus(
      autofocus: true,
      onKey: onKey,
      child: FutureBuilder<PicImage?>(
        future: _picsModel.getImage(0),
        builder: (BuildContext context, AsyncSnapshot<PicImage?> snapshot) {
          if (snapshot.hasData) {
            return StaggeredGridView.extentBuilder(
              maxCrossAxisExtent: 200,
              crossAxisSpacing: 5,
              mainAxisSpacing: 3,
              itemCount: _picsModel.getTotalImages(),
              itemBuilder: (context, index) => ImageTile(index, _picsModel),
              staggeredTileBuilder: (index) =>
                  const StaggeredTile.extent(1, 150),
            );
          } else if (snapshot.hasError)
            return Text('Error:${snapshot.error}');
          else
            return CircularProgressIndicator();
        },
      ),
    );
  }

  onKey(FocusNode node, RawKeyEvent event) {
    PicsModel _picsModel = Provider.of<PicsModel>(context, listen: false);
    _picsModel.ctrlDown = event.isControlPressed;
    _picsModel.shiftDown = event.isShiftPressed;
    Logger().i("ctrl key down ${event.isControlPressed} ");
    return true;
  }
}
