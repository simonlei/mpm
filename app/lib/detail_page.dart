import 'package:app/config.dart';
import 'package:app/pics_model.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:logger/logger.dart';
import 'package:tuple/tuple.dart';

class DetailPage extends StatefulWidget {
  const DetailPage(this.arguments);

  final Tuple2 arguments;

  @override
  State<StatefulWidget> createState() {
    return _DetailPageState(arguments);
  }
}

class _DetailPageState extends State<DetailPage> {
  late PicsModel _picsModel;
  late int _index;

  _DetailPageState(Tuple2 arguments) {
    _picsModel = arguments.item1;
    _index = arguments.item2;
  }

  @override
  Widget build(BuildContext context) {
    Logger().i('params is $_index');
    return Shortcuts(
      shortcuts: {
        LogicalKeySet(LogicalKeyboardKey.arrowLeft): NextPageIntent(-1),
        LogicalKeySet(LogicalKeyboardKey.arrowRight): NextPageIntent(1),
      },
      child: Actions(
        actions: {
          NextPageIntent: NextPageAction(this),
        },
        child: Focus(
          autofocus: true,
          child: FutureBuilder<PicImage?>(
            future: _picsModel.getImage(_index),
            builder: (BuildContext context, AsyncSnapshot<PicImage?> snapshot) {
              if (snapshot.hasData) {
                return Center(
                  child: Image(
                      image:
                          NetworkImage(Config.imageUrl(snapshot.data!.name))),
                );
              } else if (snapshot.hasError)
                return Text('Error:${snapshot.error}');
              else
                return CircularProgressIndicator();
            },
          ),
        ),
      ),
    );
  }

  void showNext(int next) {
    setState(() {
      _index += next;
    });
  }
}

class NextPageIntent extends Intent {
  const NextPageIntent(this.next);

  final int next;
}

class NextPageAction extends Action<NextPageIntent> {
  NextPageAction(this._detailPageState);

  _DetailPageState _detailPageState;

  @override
  Object? invoke(covariant NextPageIntent intent) {
    Logger().i("intent: ${intent.next}");
    _detailPageState.showNext(intent.next);
  }
}
