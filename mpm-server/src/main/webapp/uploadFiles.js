var protocol = location.protocol === 'https:' ? 'https:' : 'http:';
var prefix = protocol + '//' + bucket + '.cos.' + region + '.myqcloud.com/';

// 初始化实例
var cos = new COS({
  getAuthorization: function (options, callback) {
    // 异步获取临时密钥
    var xhr = new XMLHttpRequest();
    xhr.open('GET', '/tmpCredential', true);

    xhr.onload = function (e) {
      var credentials;
      try {
        data = (new Function('return ' + xhr.responseText))();
        credentials = data.credentials;
      } catch (e) {
      }
      if (credentials) {
        if (!credentials) {
          return console.error('credentials invalid');
        }
        callback({
          TmpSecretId: credentials.tmpSecretId,
          TmpSecretKey: credentials.tmpSecretKey,
          XCosSecurityToken: credentials.sessionToken,
          // 建议返回服务器时间作为签名的开始时间，避免用户浏览器本地时间偏差过大导致签名错误
          StartTime: data.startTime, // 时间戳，单位秒，如：1580000000
          ExpiredTime: data.expiredTime, // 时间戳，单位秒，如：1580000900
        });
      } else {
        console.error(xhr.responseText);
        callback('获取签名出错');
      }
    };
    xhr.onerror = function (e) {
      callback('获取签名出错');
    };
    xhr.send();
  }
});

function uploadFiles(files) {
  var cosFiles = [];
  var id = Date.now();
  for (var i = 0; i < files.length; i++) {
    file = files[i];
    if (file.type.startsWith("image") || file.type.startsWith("video")) {
      cosFiles.push({
        Bucket: bucket,
        Region: region,
        Key: 'upload/' + id + "_" + file.webkitRelativePath,
        Body: file
      })
    }
  }
  var count = 0;
  cos.uploadFiles({
    files: cosFiles,
    SliceSize: 1024 * 1024,
    onProgress: function (info) {
      var percent = parseInt(info.percent * 10000) / 100;
      var speed = parseInt(info.speed / 1024 / 1024 * 100) / 100;
      isc.notify('进度：' + percent + '%; 速度：' + speed + 'Mb/s;');
    },
    onFileFinish: function (err, data, options) {
      count++;
      if (count == files.length) {
        realodLeftTab();
        realodPicsGrid();
      }
      // log error file

      var httpRequest = new XMLHttpRequest();//第一步：创建需要的对象
      httpRequest.open('POST', '/uploadFile', true); //第二步：打开连接/***发送json格式文件必须设置请求头 ；如下 - */
      httpRequest.setRequestHeader("Content-type", "application/json");//设置请求头 注：post方式必须设置请求头（在建立连接后设置请求头）var obj = { name: 'zhansgan', age: 18 };
      httpRequest.send(
          JSON.stringify({key: options.Key, err: err, data: data}));//发送请求 将json写入send中
      /**
       * 获取数据后的处理程序
       */
      httpRequest.onreadystatechange = function () {//请求后的回调接口，可将请求成功后要执行的程序写在其中
        if (httpRequest.readyState == 4 && httpRequest.status == 200) {//验证请求是否发送成功
          var json = httpRequest.responseText;//获取到服务端返回的数据
          console.log(json);
          isc.notify(
              options.Key.split("\/").pop() + '上传' + (err ? '失败' : '完成'));
        }
      };
    },
  }, function (err, data) {
    console.log(err || data);
    // to refresh
    // isc_LeftTabSet_0.getSGWTInstance().reloadData_0_g$();
    // isc_PicsGrid_0.getSGWTInstance().reloadData_0_g$();

    //                         LeftTabSet.instance.reloadData();
    //                         PicsGrid.reloadData();
  });
  /*
  var total = files.length;
  var count = 0;
  var finished = 0;
  for (var i = 0; i < total; i++) {
    file = files[i];
    if (file.type.startsWith("image") || file.type.startsWith("video")) {
      count++;
      cos.putObject({
        Bucket: bucket,
        Region: region,
        Key: 'upload/' + file.webkitRelativePath,
        StorageClass: 'STANDARD',
        Body: file,
        onProgress: function (progressData) {
          if (progressData.percent > 0) {
            // {"loaded":3453674,"total":3453674,"speed":3067206.04,"percent":1}
            isc.notify("上传文件 " + file.name + " 进度 " +
                (progressData.percent * 100).toFixed(2)
                + "%，速度 " + (progressData.speed / 1024 / 1024).toFixed(2)
                + "MB"); // , [], 'message', {duration: 0.1});
          }
          if (progressData.percent == 1) {
            finished++;
            if (finished == count) {
              isc.notify("done..." + count);
            }
          }
          console.log(JSON.stringify(progressData));
        }
      }, function (err, data) {

        console.log(err || data);
      });
    }
    // callback file
    // isc.notify("uploading..." + file + " " + file.type, [], 'message',{duration: 1});

}   */

}