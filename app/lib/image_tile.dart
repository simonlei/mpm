import 'package:app/config.dart';
import 'package:app/pics_model.dart';
import 'package:flutter/material.dart';
import 'package:transparent_image/transparent_image.dart';

class ImageTile extends StatefulWidget {
  ImageTile(this.index, this.picsModel, this.onSelect);

  final ValueChanged<Set<int>> onSelect;
  final int index;
  final PicsModel picsModel;

  @override
  State<StatefulWidget> createState() {
    return _ImageTileState();
  }
}

class _ImageTileState extends State<ImageTile> {
  static const UnSelectBorder = Color(0xFF000000);
  static const SelectedBorder = Color(0xFFFF0000);

  @override
  Widget build(BuildContext context) {
    var image = widget.picsModel.getImage(widget.index);
    return GestureDetector(
      onTap: () {
        var set = Set<int>();
        set.add(widget.index);
        widget.onSelect(set);
      },
      onDoubleTap: () {
        // open details
      },
      child: Container(
        decoration: BoxDecoration(
            border: Border(
          top: BorderSide(width: 1.0, color: _getColor()),
          left: BorderSide(width: 1.0, color: _getColor()),
          right: BorderSide(width: 1.0, color: _getColor()),
          bottom: BorderSide(width: 1.0, color: _getColor()),
        )),
        child: FadeInImage.memoryNetwork(
          width: image.width,
          height: image.height,
          placeholder: kTransparentImage,
          image: Config.imageUrl(image.thumb),
        ),
      ),
    );
  }

  Color _getColor() => widget.picsModel.isSelected(widget.index) == true
      ? SelectedBorder
      : UnSelectBorder;
}
