import 'package:app/model/pics_model.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
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
          return InkResponse(
            child: SvgPicture.asset(
                image.star ? "web/icons/star.svg" : "web/icons/notstar.svg",
                width: 16),
            onTap: () async {
              setState(() {
                Actions.invoke(context, StarIntent(image));
              });
            },
          );
        });
  }
}

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
