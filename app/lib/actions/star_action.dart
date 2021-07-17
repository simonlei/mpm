import 'package:app/pics_model.dart';
import 'package:flutter/material.dart';

class StarAction extends Action<StarIntent> {
  StarAction();

  @override
  Future<Object?> invoke(covariant StarIntent intent) async {
    PicImage image = intent._image;
    print('image is $image and ${image.star}');
    await image.update({'star': !image.star});
  }
}

class StarIntent extends Intent {
  StarIntent(this._image);

  final PicImage _image;
}
