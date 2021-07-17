import 'package:app/actions/star_action.dart';
import 'package:app/pics_model.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class StarButton extends StatefulWidget {
  final PicImage _image;

  StarButton(this._image);

  @override
  State<StatefulWidget> createState() {
    return _StarButtonState();
  }
}

class _StarButtonState extends State<StarButton> {
  @override
  Widget build(BuildContext context) {
    var image = widget._image;
    return Selector<PicsModel, bool>(
        selector: (context, picsModel) => image.star,
        shouldRebuild: (prev, next) => prev != next,
        builder: (context, star, child) {
          return IconButton(
            icon: Image.asset(image.star ? "web/icons/star.png" : "web/icons/notstar.png"),
            onPressed: () async {
              // await image.update({'star': !image.star});
              setState(() {
                Actions.invoke(context, StarIntent(image));
              });
            },
          );
        });
  }
}
