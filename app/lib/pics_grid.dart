import 'package:app/image_tile.dart';
import 'package:app/pics_model.dart';
import 'package:app/widgets/star_button.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';
import 'package:logger/logger.dart';
import 'package:provider/provider.dart';
import 'package:tuple/tuple.dart';

class PicsGrid extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _PicsGridState();
  }
}

class _PicsGridState extends State<PicsGrid> {
  late PicsModel _picsModel;

  @override
  Widget build(BuildContext context) {
    return Actions(
      actions: {
        StarIntent: StarAction(),
      },
      child: Focus(
        autofocus: true,
        onKey: onKey,
        child: Consumer<PicsModel>(
          builder: (context, picsModel, child) {
            Logger().i('Consumer picsModel ${picsModel.getTotalImages()}');
            _picsModel = picsModel;
            _gridView = buildStaggeredGridView(picsModel);
            return _gridView;
          },
        ),
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
    // PicsModel _picsModel = Provider.of<PicsModel>(context, listen: false);
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
      Navigator.of(context).pushNamed('/detail', arguments: Tuple2(_picsModel, _picsModel.getSelectedIndex()));
    } else if (event.isKeyPressed(LogicalKeyboardKey.keyD)) {
      _picsModel.trashSelected();
    } else if (event.isKeyPressed(LogicalKeyboardKey.keyR)) {
      _picsModel.rotateSelected();
    } else if (event.isKeyPressed(LogicalKeyboardKey.keyS)) {
      _picsModel.starSelected();
    }
    return true;
  }
}
