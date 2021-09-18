import 'package:app/model/config.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_map_marker_cluster/flutter_map_marker_cluster.dart';
import 'package:latlong2/latlong.dart';
import 'package:transparent_image/transparent_image.dart';

class MapPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _MapPageState();
  }
}

class _MapPageState extends State<MapPage> {
  Future<List<Marker>> loadMarkers() async {
    var resp = await Dio().post(Config.api("/api/loadMarkers"));
    if (resp.statusCode == 200) {
      var photos = resp.data as List;
      List<Marker> markers = [];
      for (int i = 0; i < photos.length; i++) {
        var p = photos[i];
        markers.add(Marker(
            builder: (BuildContext context) {
              return FadeInImage.memoryNetwork(
                width: p['width'],
                height: p['height'],
                placeholder: kTransparentImage,
                image: Config.imageUrl(p['thumb']),
              );
            },
            point: LatLng(p['latitude'], p['longitude'])));
      }
      return markers;
    } else
      return [];
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<List<Marker>>(
      future: loadMarkers(),
      builder: (BuildContext context, AsyncSnapshot<List<Marker>> snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          return makeFlutterMap(snapshot.data!);
        } else if (snapshot.hasError)
          return Text('Error:${snapshot.error}');
        else
          return CircularProgressIndicator();
      },
    );
  }

  FlutterMap makeFlutterMap(List<Marker> markers) {
    return FlutterMap(
      options: new MapOptions(
        center: LatLng(31.27657805796395, 107.78060238436112),
        zoom: 5,
        plugins: [
          MarkerClusterPlugin(),
        ],
      ),
      layers: [
        TileLayerOptions(
          urlTemplate: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
          subdomains: ['a', 'b', 'c'],
        ),
        MarkerClusterLayerOptions(
          maxClusterRadius: 120,
          size: Size(40, 40),
          fitBoundsOptions: FitBoundsOptions(
            padding: EdgeInsets.all(50),
          ),
          markers: markers,
          polygonOptions: PolygonOptions(borderColor: Colors.blueAccent, color: Colors.black12, borderStrokeWidth: 3),
          builder: (context, markers) {
            return FloatingActionButton(
              child: Text(markers.length.toString()),
              onPressed: null,
            );
          },
        ),
      ],
    );
  }
}
