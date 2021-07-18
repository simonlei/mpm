import 'package:app/detail_page.dart';
import 'package:app/pics_model.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';

class RotateButton extends StatelessWidget {
  final PicImage _image;

  RotateButton(this._image);

  @override
  Widget build(BuildContext context) {
    return InkResponse(
      child: SvgPicture.asset("web/icons/rotate.svg", width: 16),
      onTap: () async {
        Actions.invoke(context, RotateIntent(_image));
        // await _image.picsModel.rotateImage(_image);
      },
    );
  }
}
