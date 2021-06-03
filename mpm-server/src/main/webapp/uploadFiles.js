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
      console.log('进度：' + percent + '%; 速度：' + speed + 'Mb/s;');
      //isc.notify('进度：' + percent + '%; 速度：' + speed + 'Mb/s;');
    },
    onFileFinish: function (err, data, options) {
      var httpRequest = new XMLHttpRequest();
      httpRequest.open('POST', '/uploadFile', true);
      httpRequest.setRequestHeader("Content-type", "application/json");
      httpRequest.send(
          JSON.stringify({key: options.Key, err: err, data: data}));
      /**
       * 获取数据后的处理程序
       */
      httpRequest.onreadystatechange = function () {//请求后的回调接口，可将请求成功后要执行的程序写在其中
        if (httpRequest.readyState == 4 && httpRequest.status == 200) {//验证请求是否发送成功
          var json = httpRequest.responseText;//获取到服务端返回的数据
          console.log(json);
          isc.notify("第 " + count + "/" + files.length + "个文件" +
              options.Key.split("\/").pop() + '上传' + (err ? '失败' : '完成'));
          count++;
          if (count == files.length) {
            realodPicsGrid();
            isc.say("上传完成，共 " + count + " 张照片");
          }
        }
      };
    },
  }, function (err, data) {
    console.log(err || data);
  });
}