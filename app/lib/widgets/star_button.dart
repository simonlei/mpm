import 'package:app/pics_model.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class StarButton extends StatefulWidget {
  PicImage _image;

  StarButton(this._image);

  @override
  State<StatefulWidget> createState() {
    return _StarButtonState();
  }
}

class _StarButtonState extends State<StarButton> {
  bool _star = false;

  @override
  Widget build(BuildContext context) {
    var image = widget._image;
    _star = image.star;
    return IconButton(
      icon: Image.asset(_star ? "web/icons/star.png" : "web/icons/notstar.png"),
      onPressed: () async {
        await image.update({'star': !image.star});
        setState(() {
          _star = image.star;
        });
      },
    );
  }
}
