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
  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: Image.asset(widget._image.star ? "web/icons/star.png" : "web/icons/notstar.png"),
      onPressed: () {
        //TODO: togger star
      },
    );
  }
}
