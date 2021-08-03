import 'dart:html';
import 'dart:typed_data';

import 'package:app/config.dart';
import 'package:app/pics_model.dart';
import 'package:app/video_view.dart';
import 'package:app/widgets/rotate_button.dart';
import 'package:app/widgets/star_button.dart';
import 'package:expire_cache/expire_cache.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:js/js_util.dart';
import 'package:logger/logger.dart';
import 'package:tuple/tuple.dart';

import 'js_wrap/heic2any.dart';

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
  ScrollController _verticalController = new ScrollController();
  ScrollController _horizontalController = new ScrollController();

  _DetailPageState(Tuple2 arguments) {
    _picsModel = arguments.item1;
    _index = arguments.item2;
  }

  @override
  void dispose() {
    _verticalController.dispose();
    _horizontalController.dispose();
    super.dispose();
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
        LogicalKeySet(LogicalKeyboardKey.keyS): StarIntent(_picsModel.imageAt(_index)!),
        LogicalKeySet(LogicalKeyboardKey.keyR): RotateIntent(_picsModel.imageAt(_index)!),
      },
      child: Actions(
        actions: {
          NextPageIntent: NextPageAction(this),
          EscapeIntent: EscapeAction(this),
          DeleteIntent: DeleteAction(this),
          ZoomIntent: ZoomAction(this),
          MoveIntent: MoveAction(this),
          StarIntent: StarAction(),
          RotateIntent: RotateAction(this),
        },
        child: Card(
          child: Focus(
            autofocus: true,
            child: FutureBuilder<PicImage?>(
              future: _loadImage(),
              builder: (BuildContext context, AsyncSnapshot<PicImage?> snapshot) {
                if (snapshot.connectionState == ConnectionState.done) {
                  var image = snapshot.data!;
                  if (image.mediaType == MediaType.video) {
                    _scale = true;
                    return wrapStack(VideoView(image), image);
                  }
                  return wrapStack(makeImageView(context, image), image);
                } else if (snapshot.hasError)
                  return Text('Error:${snapshot.error}');
                else
                  return CircularProgressIndicator();
              },
            ),
          ),
        ),
      ),
    );
  }

  late var _loadedImage;

  Future<PicImage?> _loadImage() async {
    var image = await _picsModel.getImage(_index);
    print('getImage');
    _loadedImage = await _loadRealImage(image);
    print('loadImage');
    return image;
  }

  static final ExpireCache<String, Uint8List> _cache = ExpireCache(
    expireDuration: Duration(minutes: 60),
    sizeLimit: 10,
  );

  _loadRealImage(PicImage? image) async {
    var imgName = image!.name;
    if (!_cache.isKeyInFlightOrInCache(imgName)) {
      _cache.markAsInFlight(imgName);
    } else {
      return await _cache.get(imgName);
    }
    // var response = await Dio().get(Config.imageUrl(imgName), options: Options(responseType: ResponseType.bytes));
    await HttpRequest.request(Config.imageUrl('small/$imgName'), responseType: 'blob').then((HttpRequest resp) {
      print('length: ${resp.runtimeType} ${resp.responseType} ${resp.response.runtimeType} ');
      print('content type: ${resp.responseHeaders['content-type']}');
      Blob blob = resp.response as Blob;
      print('blob length ${blob.size}');
      if (resp.responseHeaders['content-type'] != 'image/heic' &&
          resp.responseHeaders['content-type'] != 'image/heif') {
        blobReader(imgName).readAsArrayBuffer(blob);
      } else {
        var result = heic2any(HeicParams(blob: resp.response));
        print('after heic2any $result');
        var future = promiseToFuture(result);
        print('done');
        print('$future : ${future.runtimeType}');
        future.onError((error, stackTrace) {
          print('$error');
          print('$stackTrace');
        }).then((value) {
          print('Value is $value');
          var blob = value;
          print('done2');
          print('after heic2any $blob');
          blobReader(imgName).readAsArrayBuffer(blob);
        });
      }
    });
    return _cache.get(imgName);
  }

  FileReader blobReader(String imgName) {
    var reader = FileReader();
    reader.onLoad.listen((event) {
      var img = reader.result as Uint8List;
      _cache.set(imgName, img);
    });
    return reader;
  }

  SingleChildScrollView makeImageView(BuildContext context, PicImage image) {
    return SingleChildScrollView(
      controller: _verticalController,
      scrollDirection: Axis.vertical,
      child: SingleChildScrollView(
        controller: _horizontalController,
        scrollDirection: Axis.horizontal,
        child: Container(
          width: _scale ? MediaQuery.of(context).size.width : image.width,
          height: _scale ? MediaQuery.of(context).size.height : image.height,
          child: Tooltip(
            message: image.getTooltip(),
            child: RotatedBox(
              quarterTurns: image.getQuarterTurns(),
              child: Image.memory(
                _loadedImage,
                //Config.imageUrl(image.name),
                fit: _scale ? BoxFit.scaleDown : BoxFit.none,
                //placeholder: kTransparentImage,
              ),
            ),
          ),
        ),
      ),
    );
  }

  void showNext(int next) {
    setState(() {
      _scale = true;
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

  void scroll(int scrollX, int scrollY) {
    if (_scale) return;
    setState(() async {
      if (scrollY == 0) {
        var delta = MediaQuery.of(context).size.width * scrollX / 100;
        await _horizontalController.animateTo(_horizontalController.offset + delta,
            duration: Duration(milliseconds: 200), curve: Curves.ease);
      } else if (scrollX == 0) {
        var delta = MediaQuery.of(context).size.height * scrollY / 100;
        await _verticalController.animateTo(_verticalController.offset + delta,
            duration: Duration(milliseconds: 200), curve: Curves.ease);
      }
    });
  }

  Widget wrapStack(Widget child, image) {
    return Stack(
      children: [
        child,
        Row(
          children: [
            StarButton(image),
            SizedBox(width: 5),
            RotateButton(image),
          ],
        ),
      ],
    );
  }
}

class MoveAction extends Action<MoveIntent> {
  MoveAction(this._detailPageState);

  _DetailPageState _detailPageState;

  @override
  Object? invoke(covariant MoveIntent intent) {
    _detailPageState.scroll(intent.scrollX, intent.scrollY);
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

class RotateAction extends Action<RotateIntent> {
  RotateAction(this._detailPage);

  State _detailPage;

  @override
  Future<Object?> invoke(covariant RotateIntent intent) async {
    PicImage image = intent._image;
    await image.picsModel.rotateImage(image);
    _detailPage.setState(() {});
  }
}

class RotateIntent extends Intent {
  RotateIntent(this._image);

  final PicImage _image;
}
