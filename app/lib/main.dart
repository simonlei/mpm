import 'package:app/config.dart';
import 'package:app/event_bus.dart';
import 'package:app/homepage.dart';
import 'package:app/pics_model.dart';
import 'package:app/signin.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:logger/logger.dart';
import 'package:provider/provider.dart';
import 'package:tuple/tuple.dart';

import 'detail_page.dart';

void main() {
  runApp(MultiProvider(
    providers: [
      ChangeNotifierProvider(create: (_) => PicsModel()),
    ],
    child: MyApp(),
  ));
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    BUS.on("countChange", (arg) {
      Logger().i("change $arg");
      SystemChrome.setApplicationSwitcherDescription(
          ApplicationSwitcherDescription(
        label: 'My Photo Manager($arg)',
        primaryColor: Theme.of(context).primaryColor.value,
      ));
    });
    return MaterialApp(
      title: 'My Photo Manager',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      onGenerateRoute: (RouteSettings settings) {
        var routes = <String, WidgetBuilder>{
          '/': (context) => JumpWidget(),
          '/home': (context) => MyHomePage(title: 'My Photo Manager'),
          '/detail': (context) => DetailPage(settings.arguments as Tuple2),
        };
        WidgetBuilder builder = routes[settings.name]!;
        return MaterialPageRoute(builder: (ctx) => builder(ctx));
      },
    );
  }
}

class JumpWidget extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return FutureBuilder<String>(
      future: getConfig(),
      builder: (context, AsyncSnapshot<String> snapshot) {
        if (snapshot.hasData && snapshot.data == 'true') {
          return MyHomePage(title: 'My Photo Manager');
        }
        return SignInScreen();
      },
    );
  }

  Future<String> getConfig() async {
    var response = await Dio().get(Config.toUrl('/api/getConfig'));
    Config.setImageBase(response.data['baseUrl']);
    return response.data['isDev'];
  }
}
