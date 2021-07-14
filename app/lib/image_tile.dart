import 'package:app/config.dart';
import 'package:app/pics_model.dart';
import 'package:app/select_model.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:transparent_image/transparent_image.dart';
import 'package:tuple/tuple.dart';

class ImageTile extends StatefulWidget {
  ImageTile(this.index, this.picsModel);

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
                widget.picsModel.select(widget.index);
              },
              onDoubleTap: () => openDetailPage(context),
              child: Selector<SelectModel, bool>(
                selector: (context, selectModel) => selectModel.isSelected(widget.index),
                shouldRebuild: (preSelected, nextSelected) => preSelected != nextSelected,
                builder: (context, selected, child) {
                  return Container(
                    decoration: BoxDecoration(
                      border: Border.all(color: selected ? Colors.blue : Colors.grey, width: 2),
                    ),
                    child: Stack(
                      children: [
                        Tooltip(
                          message: image.getTooltip(),
                          child: FadeInImage.memoryNetwork(
                            width: image.width,
                            height: image.height,
                            placeholder: kTransparentImage,
                            image: Config.imageUrl(image.thumb),
                          ),
                        ),
                        Row(
                          children: [
                            IconButton(
                              icon: Image.asset(image.star ? "web/icons/star.png" : "web/icons/notstar.png"),
                              onPressed: () {
                                //TODO: togger star
                              },
                            ),
                            image.mediaType == MediaType.video
                                ? Row(children: [
                                    Image.asset('web/icons/start.png'),
                                    Text(
                                      // image.duration == null ? '' :
                                      image.formatDuration(),
                                      style: TextStyle(fontWeight: FontWeight.w500, color: Colors.white),
                                    )
                                  ])
                                : Text(''),
                          ],
                        ),
                      ],
                    ),
                  );
                },
              ),
            );
          } else if (snapshot.hasError)
            return Text('Error:${snapshot.error}');
          else
            return CircularProgressIndicator();
        });
  }

  void openDetailPage(BuildContext context) {
    widget.picsModel.select(widget.index);
    Navigator.of(context).pushNamed('/detail', arguments: Tuple2(widget.picsModel, widget.index));
  }
}
