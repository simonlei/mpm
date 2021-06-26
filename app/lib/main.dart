import 'package:app/config.dart';
import 'package:app/detail_page.dart';
import 'package:app/homepage.dart';
import 'package:app/signin.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'MPM',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      routes: {
        '/': (context) => JumpWidget(),
        '/home': (context) => MyHomePage(title: 'My Photo Manager'),
        '/detail': (context) => DetailPage(),
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
