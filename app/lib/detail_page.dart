import 'package:app/config.dart';
import 'package:app/pics_model.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:logger/logger.dart';
import 'package:transparent_image/transparent_image.dart';
import 'package:tuple/tuple.dart';

class DetailPage extends StatefulWidget {
  const DetailPage(this.arguments);

  final Tuple2 arguments;

  @override
  State<StatefulWidget> createState() {
    return _DetailPageState(arguments);
  }
}

class _DetailPageState extends State<DetailPage> {
  late PicsModel _picsModel;
  late int _index;
  bool _scale = true;

  _DetailPageState(Tuple2 arguments) {
    _picsModel = arguments.item1;
    _index = arguments.item2;
  }

  @override
  Widget build(BuildContext context) {
    Logger().i('params is $_index');
    return Shortcuts(
      shortcuts: {
        LogicalKeySet(LogicalKeyboardKey.arrowLeft): NextPageIntent(-1),
        LogicalKeySet(LogicalKeyboardKey.arrowRight): NextPageIntent(1),
        LogicalKeySet(LogicalKeyboardKey.escape): EscapeIntent(),
        LogicalKeySet(LogicalKeyboardKey.keyD): DeleteIntent(),
        LogicalKeySet(LogicalKeyboardKey.keyI): ZoomIntent(),
        LogicalKeySet(LogicalKeyboardKey.keyJ): MoveIntent(0, 50),
        LogicalKeySet(LogicalKeyboardKey.keyK): MoveIntent(0, -50),
        LogicalKeySet(LogicalKeyboardKey.keyL): MoveIntent(50, 0),
        LogicalKeySet(LogicalKeyboardKey.keyH): MoveIntent(-50, 0),
      },
      child: Actions(
        actions: {
          NextPageIntent: NextPageAction(this),
          EscapeIntent: EscapeAction(this),
          DeleteIntent: DeleteAction(this),
          ZoomIntent: ZoomAction(this),
          MoveIntent: MoveAction(this),
        },
        child: Focus(
          autofocus: true,
          child: FutureBuilder<PicImage?>(
            future: _picsModel.getImage(_index),
            builder: (BuildContext context, AsyncSnapshot<PicImage?> snapshot) {
              if (snapshot.hasData) {
                return Tooltip(
                  message: snapshot.data!.getTooltip(),
                  child: Container(
                    width: MediaQuery.of(context).size.width,
                    child: Container(
                      child: FadeInImage.memoryNetwork(
                          fit: _scale ? BoxFit.scaleDown : BoxFit.none,
                          placeholder: kTransparentImage,
                          image: Config.imageUrl(snapshot.data!.name)),
                    ),
                  ),
                );
              } else if (snapshot.hasError)
                return Text('Error:${snapshot.error}');
              else
                return CircularProgressIndicator();
            },
          ),
        ),
      ),
    );
  }

  void showNext(int next) {
    setState(() {
      _index += next;
      if (_index > _picsModel.getTotalImages() - 1) _index = _picsModel.getTotalImages() - 1;
      if (_index < 0) _index = 0;
      if (_picsModel.getTotalImages() == 0) Navigator.pop(context);
    });
  }

  void zoom() {
    setState(() {
      _scale = !_scale;
    });
  }
}

class MoveAction extends Action<MoveIntent> {
  MoveAction(this._detailPageState);

  _DetailPageState _detailPageState;

  @override
  Object? invoke(covariant MoveIntent intent) {
    // TODO: implement invoke
    throw UnimplementedError();
  }
}

class MoveIntent extends Intent {
  final int scrollX;
  final int scrollY;

  MoveIntent(this.scrollX, this.scrollY);
}

class ZoomAction extends Action<ZoomIntent> {
  ZoomAction(this._detailPageState);

  _DetailPageState _detailPageState;

  @override
  Object? invoke(covariant ZoomIntent intent) {
    _detailPageState.zoom();
  }
}

class ZoomIntent extends Intent {}

class DeleteAction extends Action<DeleteIntent> {
  DeleteAction(this._detailPageState);

  _DetailPageState _detailPageState;

  @override
  Future<Object?> invoke(covariant DeleteIntent intent) async {
    // Logger().d("delete ...");
    await _detailPageState._picsModel.trashSelected();
    _detailPageState._picsModel.selectNext(0);
    _detailPageState.showNext(0);
  }
}

class DeleteIntent extends Intent {}

class EscapeAction extends Action<EscapeIntent> {
  EscapeAction(this._detailPageState);

  _DetailPageState _detailPageState;

  @override
  Object? invoke(covariant EscapeIntent intent) {
    Navigator.pop(_detailPageState.context);
  }
}

class EscapeIntent extends Intent {}

class NextPageIntent extends Intent {
  const NextPageIntent(this.next);

  final int next;
}

class NextPageAction extends Action<NextPageIntent> {
  NextPageAction(this._detailPageState);

  _DetailPageState _detailPageState;

  @override
  Object? invoke(covariant NextPageIntent intent) {
    Logger().i("intent: ${intent.next}");
    _detailPageState.showNext(intent.next);
  }
}
