import 'package:app/center/images_context_menu.dart';
import 'package:app/center/photo_edit_form.dart';
import 'package:app/center/photo_view.dart';
import 'package:app/center/video_view.dart';
import 'package:app/model/config.dart';
import 'package:app/model/pics_model.dart';
import 'package:app/widgets/rotate_button.dart';
import 'package:app/widgets/star_button.dart';
import 'package:context_menus/context_menus.dart';
import 'package:dio/dio.dart';
import 'package:expire_cache/expire_cache.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:http/http.dart' as http;
import 'package:logger/logger.dart';
import 'package:plugins/plugins.dart';
import 'package:tuple/tuple.dart';
import 'package:url_launcher/url_launcher.dart';

class DetailPage extends StatefulWidget {
  const DetailPage(Key? key, this.arguments) : super(key: key);

  final Tuple2 arguments;

  @override
  State<StatefulWidget> createState() {
    return DetailPageState(arguments);
  }
}

final GlobalKey<PhotoViewState> photoViewKey = GlobalKey<PhotoViewState>();

class DetailPageState extends State<DetailPage> {
  late PicsModel _picsModel;
  late int _index;
  bool _scale = true;
  late FocusNode focusNode;
  final _plugins = Plugins();

  DetailPageState(Tuple2 arguments) {
    _picsModel = arguments.item1;
    _index = arguments.item2;
  }

  @override
  void initState() {
    super.initState();
    focusNode = new FocusNode();
  }

  @override
  void dispose() {
    focusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    Logger().i('params is $_index');
    return Card(
      child: FutureBuilder<PicImage?>(
        future: _loadImage(),
        builder: (BuildContext context, AsyncSnapshot<PicImage?> snapshot) {
          return makeDetailWidget(snapshot, context);
        },
      ),
    );
  }

  Widget makeDetailWidget(
      AsyncSnapshot<PicImage?> snapshot, BuildContext context) {
    if (snapshot.connectionState == ConnectionState.done &&
        snapshot.data != null) {
      var image = snapshot.data!;
      return Row(
        children: [
          Expanded(
            child: Shortcuts(
              shortcuts: {
                LogicalKeySet(LogicalKeyboardKey.arrowLeft): NextPageIntent(-1),
                LogicalKeySet(LogicalKeyboardKey.arrowRight): NextPageIntent(1),
                LogicalKeySet(LogicalKeyboardKey.escape): EscapeIntent(),
                LogicalKeySet(LogicalKeyboardKey.keyC): CopyIntent(),
                LogicalKeySet(LogicalKeyboardKey.keyD): DeleteIntent(),
                LogicalKeySet(LogicalKeyboardKey.keyI): ZoomIntent(),
                LogicalKeySet(LogicalKeyboardKey.keyJ): MoveIntent(0, 50),
                LogicalKeySet(LogicalKeyboardKey.keyK): MoveIntent(0, -50),
                LogicalKeySet(LogicalKeyboardKey.keyL): MoveIntent(50, 0),
                LogicalKeySet(LogicalKeyboardKey.keyH): MoveIntent(-50, 0),
                LogicalKeySet(LogicalKeyboardKey.keyS):
                    StarIntent(_picsModel.imageAt(_index)!),
                LogicalKeySet(LogicalKeyboardKey.keyR):
                    RotateIntent(_picsModel.imageAt(_index)!),
              },
              child: Actions(
                actions: {
                  CopyIntent: CopyAction(this),
                  NextPageIntent: NextPageAction(this),
                  EscapeIntent: EscapeAction(this),
                  DeleteIntent: DeleteAction(this),
                  ZoomIntent: ZoomAction(this),
                  MoveIntent: MoveAction(),
                  StarIntent: StarAction(),
                  RotateIntent: RotateAction(this),
                },
                child: Focus(
                  autofocus: true,
                  focusNode: focusNode,
                  child: GestureDetector(
                    onTap: () {
                      focusNode.requestFocus();
                    },
                    child: wrapDetail(image, context),
                  ),
                ),
              ),
            ),
          ),
          PhotoEditForm(image),
        ],
      );
    } else if (snapshot.hasError)
      return Text('Error:${snapshot.error}');
    else
      return CircularProgressIndicator();
  }

  wrapDetail(PicImage image, BuildContext context) {
    if (image.mediaType == MediaType.video) {
      _scale = true;
      return wrapStack(VideoView(image), image);
    }
    return wrapStack(
        PhotoView(photoViewKey, image, _scale, _loadedImage, _loadedImageInfo),
        image);
  }

  late var _loadedImage;
  late var _loadedImageInfo;

  Future<PicImage?> _loadImage() async {
    var image = await _picsModel.getImage(_index);
    _loadedImage = await _loadRealImage(image!.name, image.rotate);
    _loadedImageInfo = await _loadImageInfo(image, _loadedImage);
    print('loadImage');
    preloadImage(_index + 1);
    print('load next Image');
    return image;
  }

  Future<void> preloadImage(int i) async {
    var image = await _picsModel.getImage(i);
    if (image == null) return;
    await _loadRealImage(image.name, image.rotate);
    await _loadImageInfo(image, _loadedImage);
  }

  static final ExpireCache<String, Uint8List> _cache = ExpireCache(
    expireDuration: Duration(minutes: 60),
    sizeLimit: 10,
  );
  static final ExpireCache<String, ImageInfo> _infoCache = ExpireCache(
    expireDuration: Duration(minutes: 60),
    sizeLimit: 10,
  );

  _loadRealImage(String imgName, int rotate) async {
    if (!_cache.isKeyInFlightOrInCache(imgName)) {
      print("not in flight");
      _cache.markAsInFlight(imgName);
    } else {
      print("cached");
      return await _cache.get(imgName);
    }
    var strip = rotate == 3600 ? '' : '?imageMogr2/strip';
    print(Config.imageUrl('small/$imgName$strip'));
    await http
        .get(Uri.parse(Config.imageUrl('small/$imgName$strip')))
        .then((http.Response resp) async {
      print('length: ${resp.runtimeType} ${resp.contentLength}');
      print('content type: ${resp.headers['content-type']}');

      var blob = resp.bodyBytes;

      print('blob length ${blob.length}');
      if (resp.headers['content-type'] != 'image/heic' &&
          resp.headers['content-type'] != 'image/heif') {
        _cache.set(imgName, blob);
      } else {
        var result = await _plugins.heic2jpg(blob) ?? blob;
        print('after heic2any $result');
        _cache.set(imgName, result);
      }
    }).catchError((e) {
      print('err ${e.toString()}');
    });
    return _cache.get(imgName);
  }

  Future<ImageInfo?> _loadImageInfo(
      PicImage image, Uint8List _loadedImage) async {
    String imgName = image.name;
    if (!_infoCache.isKeyInFlightOrInCache(imgName)) {
      _infoCache.markAsInFlight(imgName);
    } else {
      return await _infoCache.get(imgName);
    }

    bool vertical = false;
    if (image.rotate == 3600) {
      try {
        var response = await Dio().get(Config.imageUrl('small/$imgName?exif'));
        vertical = int.parse(response.data['Orientation']['val']) > 4;
      } catch (e) {}
    }

    var response = await Dio().get(Config.imageUrl('small/$imgName?imageInfo'));
    var width = double.parse(response.data['width']);
    var height = double.parse(response.data['height']);

    var imageInfo = ImageInfo(
        vertical ? height : width, vertical ? width : height, vertical);
    _infoCache.set(imgName, imageInfo);
    return imageInfo;
  }

  void showNext(int next) {
    setState(() {
      _scale = true;
      _index += next;
      if (_index > _picsModel.getTotalImages() - 1)
        _index = _picsModel.getTotalImages() - 1;
      if (_index < 0) _index = 0;
      if (_picsModel.getTotalImages() == 0) Navigator.pop(context);
    });
  }

  void zoom() {
    setState(() {
      _scale = !_scale;
    });
  }

  Widget wrapStack(Widget child, image) {
    print('pic model is $_picsModel');
    return ContextMenuRegion(
      contextMenu: ImagesContextMenu(_picsModel, true),
      child: Stack(
        children: [
          child,
          Row(
            children: [
              StarButton(image),
              SizedBox(width: 5),
              RotateButton(image),
              InkResponse(
                child: SvgPicture.asset("web/icons/download.svg", width: 16),
                onTap: () => launchUrl(Config.imageUrl('origin/${image.name}')),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Future<void> clearCache(String name) async {
    await _cache.invalidate(name);
    await _infoCache.invalidate(name);
    setState(() {});
  }
}

class MoveAction extends Action<MoveIntent> {
  @override
  Object? invoke(covariant MoveIntent intent) {
    if (photoViewKey.currentState != null)
      photoViewKey.currentState!.scroll(intent.scrollX, intent.scrollY);
    return null;
  }
}

class MoveIntent extends Intent {
  final int scrollX;
  final int scrollY;

  MoveIntent(this.scrollX, this.scrollY);
}

class ZoomAction extends Action<ZoomIntent> {
  ZoomAction(this._detailPageState);

  DetailPageState _detailPageState;

  @override
  Object? invoke(covariant ZoomIntent intent) {
    _detailPageState.zoom();
    return null;
  }
}

class ZoomIntent extends Intent {}

class DeleteAction extends Action<DeleteIntent> {
  DeleteAction(this._detailPageState);

  DetailPageState _detailPageState;

  @override
  Future<Object?> invoke(covariant DeleteIntent intent) async {
    // Logger().d("delete ...");
    await _detailPageState._picsModel.trashSelected();
    _detailPageState._picsModel.selectNext(0);
    _detailPageState.showNext(0);
    return null;
  }
}

class DeleteIntent extends Intent {}

class EscapeAction extends Action<EscapeIntent> {
  EscapeAction(this._detailPageState);

  DetailPageState _detailPageState;

  @override
  Object? invoke(covariant EscapeIntent intent) {
    Navigator.pop(_detailPageState.context);
    return null;
  }
}

class EscapeIntent extends Intent {}

class CopyIntent extends Intent {}

class CopyAction extends Action<CopyIntent> {
  CopyAction(this._detailPageState);

  DetailPageState _detailPageState;

  @override
  Future<Object?> invoke(covariant CopyIntent intent) async {
    var picImage =
        await _detailPageState._picsModel.getImage(_detailPageState._index);
    await Clipboard.setData(
        ClipboardData(text: Config.imageUrl('small/${picImage!.name}')));
    return null;
  }
}

class NextPageIntent extends Intent {
  const NextPageIntent(this.next);

  final int next;
}

class NextPageAction extends Action<NextPageIntent> {
  NextPageAction(this._detailPageState);

  DetailPageState _detailPageState;

  @override
  Object? invoke(covariant NextPageIntent intent) {
    Logger().i("intent: ${intent.next}");
    _detailPageState._picsModel.selectNext(intent.next);
    _detailPageState.showNext(intent.next);
    return null;
  }
}

class RotateAction extends Action<RotateIntent> {
  RotateAction(this._detailPage);

  State _detailPage;

  @override
  Future<Object?> invoke(covariant RotateIntent intent) async {
    PicImage image = intent._image;
    await image.picsModel.rotateImage(image);
    if (_detailPage is DetailPageState)
      (_detailPage as DetailPageState).clearCache(image.name);
    return null;
  }
}

class RotateIntent extends Intent {
  RotateIntent(this._image);

  final PicImage _image;
}

class ImageInfo {
  ImageInfo(this.width, this.height, this.vertical);

  double scaledWidth(_context) {
    var screenWidth = MediaQuery.of(_context).size.width - 250;
    print('screen width is $screenWidth');
    if (!vertical) return screenWidth;
    var screenHeight = MediaQuery.of(_context).size.height;
    if (width / height > screenWidth / screenHeight) return screenWidth;
    return width * screenHeight / height;
  }

  double scaledHeight(_context) {
    var screenHeight = MediaQuery.of(_context).size.height;
    if (!vertical) return screenHeight;
    var screenWidth = MediaQuery.of(_context).size.width - 250;
    if (width / height < screenWidth / screenHeight) return screenHeight;
    return height * screenWidth / width;
  }

  double width;
  double height;
  bool vertical;
}
