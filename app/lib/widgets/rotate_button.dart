import 'package:app/detail_page.dart';
import 'package:app/pics_model.dart';
import 'package:flutter/material.dart';

class RotateButton extends StatelessWidget {
  final PicImage _image;

  RotateButton(this._image);

  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: Image.asset("web/icons/rotate.png"),
      onPressed: () async {
        Actions.invoke(context, RotateIntent(_image));
        // await _image.picsModel.rotateImage(_image);
      },
    );
  }
}
