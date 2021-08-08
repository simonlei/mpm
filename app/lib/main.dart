import 'package:app/homepage.dart';
import 'package:app/model/config.dart';
import 'package:app/model/event_bus.dart';
import 'package:app/model/pics_model.dart';
import 'package:app/model/select_model.dart';
import 'package:app/signin.dart';
import 'package:context_menus/context_menus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:logger/logger.dart';
import 'package:oktoast/oktoast.dart';
import 'package:provider/provider.dart';
import 'package:tuple/tuple.dart';

import 'center/detail_page.dart';

void main() {
  var selectModel = SelectModel();
  runApp(MultiProvider(
    providers: [
      ChangeNotifierProvider(create: (_) => selectModel),
      ChangeNotifierProvider(create: (_) => PicsModel(selectModel)),
    ],
    child: MyApp(),
  ));
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    BUS.on(EventBus.CountChange, (arg) {
      Logger().i("Count Change $arg");
      SystemChrome.setApplicationSwitcherDescription(
        ApplicationSwitcherDescription(
          label: 'My Photo Manager($arg)',
          primaryColor: Theme.of(context).primaryColor.value,
        ),
      );
    });
    return OKToast(
      position: ToastPosition.bottom,
      child: MaterialApp(
        title: 'My Photo Manager',
        onGenerateRoute: (RouteSettings settings) {
          var routes = <String, WidgetBuilder>{
            '/': (context) => JumpWidget(),
            '/home': (context) => MyHomePage(title: 'My Photo Manager'),
            '/detail': (context) => ContextMenuOverlay(
                  child: DetailPage(settings.arguments as Tuple2),
                ),
          };
          WidgetBuilder builder = routes[settings.name]!;
          return MaterialPageRoute(builder: (ctx) => builder(ctx));
        },
        localizationsDelegates: [
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
        ],
        supportedLocales: [
          const Locale('zh', 'CH'),
          const Locale('en', 'US'),
        ],
        locale: const Locale('zh'),
      ),
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
    var response = await Dio().get(Config.api('/api/getConfig'));
    Config.setImageBase(response.data['baseUrl']);
    Config.region = response.data['region'];
    Config.bucket = response.data['bucket'];
    return response.data['isDev'];
  }
}
