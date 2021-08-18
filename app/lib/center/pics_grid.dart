import 'package:app/center/image_tile.dart';
import 'package:app/center/images_context_menu.dart';
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
            return buildStaggeredGridView(picsModel);
          },
        ),
      ),
    );
  }

  late StaggeredGridView _gridView;
  late ScrollController _scrollController;
  final GlobalKey _gridViewKey = GlobalKey();

  ContextMenuRegion buildStaggeredGridView(PicsModel _picsModel) {
    _scrollController = ScrollController();
    _gridView = StaggeredGridView.extentBuilder(
      key: _gridViewKey,
      controller: _scrollController,
      maxCrossAxisExtent: maxCrossAxisExtent,
      crossAxisSpacing: 5,
      mainAxisSpacing: 3,
      itemCount: _picsModel.getTotalImages(),
      itemBuilder: (context, index) => ImageTile(index, _picsModel),
      staggeredTileBuilder: (index) => const StaggeredTile.extent(1, 150),
    );
    return ContextMenuRegion(
      contextMenu: ImagesContextMenu(_picsModel, false),
      child: LayoutBuilder(builder: (BuildContext context, BoxConstraints constraints) {
        return Consumer<SelectModel>(builder: (context, selectModel, child) {
          print('constrains ${constraints.maxWidth}');
          if (selectModel.lastIndex >= 0) {
            if (_scrollController.hasClients) {
              var rows = selectModel.lastIndex / getCrossAxisCount(constraints.maxWidth);
              var tileTop = rows.floor() * 150.0;
              print('row: $rows -> $tileTop');
              print('position ${_scrollController.offset} height ${constraints.maxHeight}');
              if (tileTop < _scrollController.offset ||
                  tileTop + 150 > _scrollController.offset + constraints.maxHeight)
                _scrollController.animateTo(tileTop, duration: Duration(milliseconds: 200), curve: Curves.ease);
            }
          }
          return _gridView;
        });
      }),
    );
  }

  static const maxCrossAxisExtent = 200.0;

  onKey(FocusNode node, RawKeyEvent event) {
    _picsModel.metaDown = event.isMetaPressed;
    _picsModel.shiftDown = event.isShiftPressed;
    try {
      if (_gridViewKey.currentContext != null) print('screen width ${_gridViewKey.currentContext!.size}');
    } catch (e) {
      print(e);
    }

    int crossAxisCount = getCrossAxisCount(_gridViewKey.currentContext!.size!.width);
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
}
