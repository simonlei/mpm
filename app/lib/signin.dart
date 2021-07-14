import 'package:app/config.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

class SignInScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: SizedBox(
          width: 400,
          child: Card(
            child: SignInForm(),
          ),
        ),
      ),
    );
  }
}

class SignInForm extends StatefulWidget {
  @override
  _SignUpFormState createState() => _SignUpFormState();
}

class _SignUpFormState extends State<SignInForm> {
  final _passwordController = TextEditingController();
  late String? _validatedMsg;
  final _formKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    return Form(
        key: _formKey,
        child: Column(
          children: [
            Text('请输入密码'),
            Padding(
              padding: EdgeInsets.all(8.0),
              child: TextFormField(
                obscureText: true,
                controller: _passwordController,
                decoration: InputDecoration(hintText: '密码'),
                validator: (value) {
                  return _validatedMsg;
                },
              ),
            ),
            TextButton(
              onPressed: () async {
                await validatePassword();
                if (_formKey.currentState!.validate()) {
                  Navigator.of(context).pushNamed('/home');
                }
              },
              child: Text('确定'),
            ),
          ],
        ));
  }

  Future<void> validatePassword() async {
    var value = _passwordController.value.text;
    if (value == null || value.isEmpty)
      _validatedMsg = '请输入密码';
    else {
      var response = await Dio().post(Config.api('/api/authPassword'), data: value);
      _validatedMsg = 'ok' == response.data['ok'] ? null : '密码不对，请重新输入';
    }
  }
}
