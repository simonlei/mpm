import 'package:app/model/pics_model.dart';
import 'package:flutter/material.dart';

class PhotoView extends StatefulWidget {
  final PicImage _image;
  final _loadedImage;
  final _loadedImageInfo;
  final _scale;

  const PhotoView(Key? key, this._image, this._scale, this._loadedImage, this._loadedImageInfo) : super(key: key);

  @override
  State<StatefulWidget> createState() {
    return PhotoViewState();
  }
}

class PhotoViewState extends State<PhotoView> {
  ScrollController _verticalController = new ScrollController();
  ScrollController _horizontalController = new ScrollController();

  @override
  void dispose() {
    _verticalController.dispose();
    _horizontalController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Center(
      child: SingleChildScrollView(
        controller: _verticalController,
        scrollDirection: Axis.vertical,
        child: SingleChildScrollView(
          controller: _horizontalController,
          scrollDirection: Axis.horizontal,
          child: Container(
            width: widget._scale
                ? widget._loadedImageInfo.scaledWidth(context)
                : widget._image.rotate % 180 == 90
                    ? widget._loadedImageInfo.height
                    : widget._loadedImageInfo.width,
            height: widget._scale
                ? widget._loadedImageInfo.scaledHeight(context)
                : widget._image.rotate % 180 == 90
                    ? widget._loadedImageInfo.width
                    : widget._loadedImageInfo.height,
            child: Tooltip(
              message: widget._image.getTooltip(),
              waitDuration: Duration(seconds: 2),
              child: RotatedBox(
                quarterTurns: widget._image.getQuarterTurns(),
                child: Image.memory(
                  widget._loadedImage,
                  width: widget._loadedImageInfo.width,
                  height: widget._loadedImageInfo.height,
                  fit:
                      widget._loadedImageInfo.vertical ? BoxFit.fill : (widget._scale ? BoxFit.scaleDown : BoxFit.none),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Future<void> scroll(int scrollX, int scrollY) async {
    if (widget._scale) return;

    if (scrollY == 0) {
      var delta = MediaQuery.of(context).size.width * scrollX / 100;
      await _horizontalController.animateTo(_horizontalController.offset + delta,
          duration: Duration(milliseconds: 200), curve: Curves.ease);
    } else if (scrollX == 0) {
      var delta = MediaQuery.of(context).size.height * scrollY / 100;
      await _verticalController.animateTo(_verticalController.offset + delta,
          duration: Duration(milliseconds: 200), curve: Curves.ease);
    }
  }
}
