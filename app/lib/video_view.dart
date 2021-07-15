import 'package:app/config.dart';
import 'package:app/pics_model.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';

class VideoView extends StatefulWidget {
  final PicImage _image;

  VideoView(this._image);

  @override
  State<StatefulWidget> createState() {
    return _VideoViewState(_image);
  }
}

class _VideoViewState extends State<VideoView> {
  late final VideoPlayerController _controller;

  PicImage _image;

  late Future<void> _initializeVideoPlayerFuture;

  _VideoViewState(this._image);

  @override
  void initState() {
    super.initState();
    var imageUrl = Config.imageUrl('video_t/${_image.name}.mp4');
    print("Image url is $imageUrl");
    _controller = VideoPlayerController.network(imageUrl);
    _initializeVideoPlayerFuture = _controller.initialize();
    _controller.setLooping(true);
  }

  @override
  void dispose() {
    _controller.pause();
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: MediaQuery.of(context).size.width,
      height: MediaQuery.of(context).size.height,
      child: Tooltip(
        message: _image.getTooltip(),
        child: FutureBuilder(
          future: _initializeVideoPlayerFuture,
          builder: (context, snapshot) {
            if (snapshot.connectionState == ConnectionState.done) {
              _controller.play();
              return AspectRatio(
                aspectRatio: _controller.value.aspectRatio,
                child: Stack(
                  alignment: Alignment.bottomCenter,
                  children: [
                    VideoPlayer(_controller),
                    VideoProgressIndicator(_controller, allowScrubbing: true),
                  ],
                ),
              );
            } else {
              return Center(
                child: CircularProgressIndicator(),
              );
            }
          },
        ),
      ),
    );
  }
}
