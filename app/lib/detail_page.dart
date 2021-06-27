import 'package:app/config.dart';
import 'package:app/pics_model.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:logger/logger.dart';
import 'package:tuple/tuple.dart';

class DetailPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _DetailPageState();
  }
}

class _DetailPageState extends State<DetailPage> {
  late PicsModel _picsModel;
  late int _index;

  @override
  Widget build(BuildContext context) {
    Tuple2 params = ModalRoute.of(context)!.settings.arguments as Tuple2;
    _picsModel = params.item1;
    _index = params.item2;

    Logger().i('params is $_index');
    return Shortcuts(
      shortcuts: {
        LogicalKeySet(LogicalKeyboardKey.arrowLeft): NextPageIntent(-1),
        LogicalKeySet(LogicalKeyboardKey.arrowRight): NextPageIntent(1),
      },
      child: Actions(
        actions: {
          NextPageIntent: NextPageAction(),
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
}

class NextPageIntent extends Intent {
  const NextPageIntent(this.next);

  final int next;
}

class NextPageAction extends Action<NextPageIntent> {
  @override
  Object? invoke(covariant NextPageIntent intent) {
    Logger().i("intent: ${intent.next}");
  }
}
