import 'package:flutter/material.dart';

class Header extends Row {
  Header()
      : super(children: [
          TextButton(
            child: Text('Upload'),
            onPressed: () {
              /*
              COS cos = new COS({
                'getAuthorization': allowInterop((options, callback) {
                  Logger().i("Options $options, callback $callback");
                  callback('获取签名出错');
                })
              });
              Logger().i("Cos is $cos");
              Logger().i("Cos is ${cos.uploadFiles}");

               */
              // cos.uploadFiles({}, allowInterop(() {}));
            },
          )
        ]);
}
