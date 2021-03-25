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
  var total = files.length;
  var count = 0;
  for (var i = 0; i < total; i++) {
    file = files[i];
    if (file.type.startsWith("image") || file.type.startsWith("video")) {
      cos.putObject({
        Bucket: bucket,
        Region: region,
        Key: 'upload/' + file.webkitRelativePath,
        StorageClass: 'STANDARD',
        Body: file,
        onProgress: function (progressData) {
          if (progressData.percent > 0) {
            // {"loaded":3453674,"total":3453674,"speed":3067206.04,"percent":1}
            isc.notify("上传文件 " + file.name + " 进度 " + progressData.percent * 100
                + "%，速度 " + (progressData.speed / 1024 / 1024).toFixed(2)
                + "MB", [], 'message', {duration: 0.1});
          }
          console.log(JSON.stringify(progressData));
        }
      }, function (err, data) {
        console.log(err || data);
      });
      count++;
    }
    // callback file
    // isc.notify("uploading..." + file + " " + file.type, [], 'message',{duration: 1});
  }
  isc.notify("done..." + count);
}