import 'package:app/config.dart';
import 'package:app/pics_model.dart';
import 'package:flutter/material.dart';
import 'package:transparent_image/transparent_image.dart';
import 'package:tuple/tuple.dart';

class ImageTile extends StatefulWidget {
  ImageTile(this.index, this.picsModel, this.onSelect);

  final ValueChanged<Set<int>> onSelect;
  final int index;
  final PicsModel picsModel;

  @override
  State<StatefulWidget> createState() {
    return _ImageTileState();
  }

  Future<PicImage?> getCurrentImage() {
    return picsModel.getImage(index);
  }
}

class _ImageTileState extends State<ImageTile> {
  @override
  Widget build(BuildContext context) {
    return FutureBuilder<PicImage?>(
        future: widget.getCurrentImage(),
        builder: (BuildContext context, AsyncSnapshot<PicImage?> snapshot) {
          if (snapshot.hasData) {
            PicImage image = snapshot.data!;
            return GestureDetector(
              onTap: () {
                var set = Set<int>();
                // RawKeyboard.instance.
                // RawKeyDownEvent().isControlPressed()
                set.add(widget.index);
                widget.onSelect(set);
              },
              onDoubleTap: () {
                // open details
                Navigator.of(context).pushNamed('/detail',
                    arguments: Tuple2(widget.picsModel, widget.index));
              },
              child: Container(
                decoration: BoxDecoration(
                    border: Border.all(color: _getColor(), width: 2)),
                child: FadeInImage.memoryNetwork(
                  width: image.width,
                  height: image.height,
                  placeholder: kTransparentImage,
                  image: Config.imageUrl(image.thumb),
                ),
              ),
            );
          } else if (snapshot.hasError)
            return Text('Error:${snapshot.error}');
          else
            return CircularProgressIndicator();
        });
  }

  Color _getColor() => widget.picsModel.isSelected(widget.index) == true
      ? Colors.amber
      : Colors.grey;
}
