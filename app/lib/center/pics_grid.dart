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

  ContextMenuRegion buildStaggeredGridView(PicsModel _picsModel) {
    _scrollController = ScrollController();
    _gridView = StaggeredGridView.extentBuilder(
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
      child: Consumer<SelectModel>(builder: (context, selectModel, child) {
        if (selectModel.lastIndex >= 0) {
          var rows = selectModel.lastIndex / getCrossAxisCount();
          print('row: $rows -> ${rows.floor()}');
          if (_scrollController.hasClients)
            _scrollController.animateTo(rows.floor() * 150, duration: Duration(milliseconds: 200), curve: Curves.ease);
        }
        return _gridView;
      }),
    );
  }

  static const maxCrossAxisExtent = 200.0;

  onKey(FocusNode node, RawKeyEvent event) {
    // PicsModel _picsModel = Provider.of<PicsModel>(context, listen: false);
    _picsModel.metaDown = event.isMetaPressed;
    _picsModel.shiftDown = event.isShiftPressed;

    int crossAxisCount = getCrossAxisCount();
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

  int getCrossAxisCount() {
    double crossAxisExtent = MediaQuery.of(context).size.width;
    int crossAxisCount = ((crossAxisExtent + 5) / (maxCrossAxisExtent + 5)).ceil();
    return crossAxisCount;
  }
}
