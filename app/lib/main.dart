import 'package:flutter/material.dart';

import 'homepage.dart';
import 'signin.dart';

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
        '/': (context) => SignInScreen(),
        '/home': (context) => MyHomePage(title: 'My Photo Manager')
      },
    );
  }
}
