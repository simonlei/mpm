import 'package:app/config.dart';
import 'package:flutter/material.dart';
import 'package:transparent_image/transparent_image.dart';

class ImageTile extends StatelessWidget {
  const ImageTile(this.index, this.image);

  final int index;
  final PicImage image;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () {
        // set selected
        print('hhhhhllllo${image.thumb}');
      },
      onDoubleTap: () {
        // open details
      },
      child: FadeInImage.memoryNetwork(
        width: image.width,
        height: image.height,
        placeholder: kTransparentImage,
        image: Config.imageUrl(image.thumb),
      ),
    );
  }
}

class PicImage {
  const PicImage(this.width, this.height, this.thumb);

  final double width;
  final double height;
  final String thumb;
}
