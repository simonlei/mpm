import 'package:app/image_tile.dart';
import 'package:app/pics_model.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';
import 'package:provider/provider.dart';
import 'package:tuple/tuple.dart';

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
            _gridView = buildStaggeredGridView(_picsModel);
            return _gridView;
          } else if (snapshot.hasError)
            return Text('Error:${snapshot.error}');
          else
            return CircularProgressIndicator();
        },
      ),
    );
  }

  late StaggeredGridView _gridView;

  StaggeredGridView buildStaggeredGridView(PicsModel _picsModel) {
    return StaggeredGridView.extentBuilder(
      maxCrossAxisExtent: 200,
      crossAxisSpacing: 5,
      mainAxisSpacing: 3,
      itemCount: _picsModel.getTotalImages(),
      itemBuilder: (context, index) => ImageTile(index, _picsModel),
      staggeredTileBuilder: (index) => const StaggeredTile.extent(1, 150),
    );
  }

  onKey(FocusNode node, RawKeyEvent event) {
    PicsModel _picsModel = Provider.of<PicsModel>(context, listen: false);
    _picsModel.metaDown = event.isMetaPressed;
    _picsModel.shiftDown = event.isShiftPressed;

    double crossAxisExtent = MediaQuery.of(context).size.width;
    int crossAxisCount = ((crossAxisExtent + 5) / (200 + 5)).ceil();
    if (event.isKeyPressed(LogicalKeyboardKey.arrowLeft)) {
      _picsModel.selectNext(-1);
    } else if (event.isKeyPressed(LogicalKeyboardKey.arrowRight)) {
      _picsModel.selectNext(1);
    } else if (event.isKeyPressed(LogicalKeyboardKey.arrowUp)) {
      _picsModel.selectNext(-crossAxisCount);
    } else if (event.isKeyPressed(LogicalKeyboardKey.arrowDown)) {
      _picsModel.selectNext(crossAxisCount);
    } else if (event.isKeyPressed(LogicalKeyboardKey.enter)) {
      Navigator.of(context).pushNamed('/detail',
          arguments: Tuple2(_picsModel, _picsModel.getSelectedIndex()));
    }
    return true;
  }
}
