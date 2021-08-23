import 'package:app/center/image_tile.dart';
import 'package:app/center/images_context_menu.dart';
import 'package:app/homepage.dart';
import 'package:app/model/pics_model.dart';
import 'package:app/model/select_model.dart';
import 'package:app/widgets/star_button.dart';
import 'package:context_menus/context_menus.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';
import 'package:logger/logger.dart';
import 'package:provider/provider.dart';
import 'package:tuple/tuple.dart';

class PicsGrid extends StatefulWidget {
  final BoxConstraints _constraints;

  PicsGrid(Key? key, this._constraints) : super(key: key);

  @override
  State<StatefulWidget> createState() {
    return PicsGridState();
  }
}

class PicsGridState extends State<PicsGrid> {
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
            return buildStaggeredGridView(picsModel);
          },
        ),
      ),
    );
  }

  late ScrollController _scrollController = ScrollController();

  ContextMenuRegion buildStaggeredGridView(PicsModel _picsModel) {
    return ContextMenuRegion(
        contextMenu: ImagesContextMenu(_picsModel, false),
        child: StaggeredGridView.extentBuilder(
          controller: _scrollController,
          maxCrossAxisExtent: maxCrossAxisExtent,
          crossAxisSpacing: 5,
          mainAxisSpacing: 3,
          itemCount: _picsModel.getTotalImages(),
          itemBuilder: (context, index) => ImageTile(index, _picsModel),
          staggeredTileBuilder: (index) => const StaggeredTile.extent(1, 150),
        ));
  }

  static const maxCrossAxisExtent = 200.0;

  onKey(FocusNode node, RawKeyEvent event) {
    _picsModel.metaDown = event.isMetaPressed;
    _picsModel.shiftDown = event.isShiftPressed;
    try {
      if (gridViewKey.currentContext != null) print('screen width ${gridViewKey.currentContext!.size}');
    } catch (e) {
      print(e);
    }

    int crossAxisCount = getCrossAxisCount(gridViewKey.currentContext!.size!.width);
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

  int getCrossAxisCount(double width) {
    return ((width + 5) / (maxCrossAxisExtent + 5)).ceil();
  }

  void scrollToIndex(SelectModel selectModel) {
    print(widget._constraints.maxWidth);
    int crossAxisCount = getCrossAxisCount(widget._constraints.maxWidth);
    var rows = selectModel.lastIndex / crossAxisCount;
    var tileTop = rows.floor() * 153.0;
    print('row: $rows -> $tileTop');
    print('position ${_scrollController.offset} height ${widget._constraints.maxHeight}');
    if (tileTop < _scrollController.offset || tileTop + 153 > _scrollController.offset + widget._constraints.maxHeight)
      _scrollController.jumpTo(tileTop);
  }
}
