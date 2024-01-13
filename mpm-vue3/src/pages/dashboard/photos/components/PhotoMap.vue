<template>
  <ol-map
    :loadTilesWhileAnimating="true"
    :loadTilesWhileInteracting="true"
    style="height: 100%"
  >
    <ol-view
      ref="view"
      :center="center"
      :projection="projection"
      :rotation="rotation"
      :zoom="zoom"
    />

    <ol-tile-layer>
      <ol-source-osm/>
    </ol-tile-layer>

    <ol-interaction-clusterselect
      :featureStyle="featureStyle"
      :multi="true"
      :pointRadius="100"
      @select="featureSelected"
    >
      <ol-style :overrideStyleFunction="overrideStyleFunction">
      </ol-style>
    </ol-interaction-clusterselect>

    <ol-animated-clusterlayer :animationDuration="500" :distance="200" :minimal-distance="200">
      <ol-source-vector ref="vectorsource" :features="features">
      </ol-source-vector>

      <ol-style :overrideStyleFunction="overrideStyleFunction">
        <ol-style-text backgroundFill="grey" font="30px serif"
                       offsetX="50"
                       offsetY="-50">
          <ol-style-fill color="white"></ol-style-fill>
        </ol-style-text>
      </ol-style>
    </ol-animated-clusterlayer>
  </ol-map>
</template>


<script lang="ts" setup>
import {computed, ref} from "vue";
import {Circle, Fill, Icon, Stroke, Style} from "ol/style";
import {loadMarkers} from "@/api/photos";
import {Feature} from "ol";
import {Point} from "ol/geom";
import {getPhotoThumb} from "@/api/model/photos";

const center = ref([107, 31]);
const projection = ref("EPSG:4326");
const zoom = ref(5);
const rotation = ref(0);
let markers = await loadMarkers();
console.log('markers count: {}', markers.length);

const features = computed(() => {

  return markers.map((photo, index) => {
    return new Feature({
      geometry: new Point([photo.longitude, photo.latitude]),
      photo: photo,
    });
  });
});
console.log('features length is {} ', features.value.length);


// style of the "artificial" item markers and lines connected to the cluster base after first click on the cluster -->
const featureStyle = (feature) => {
  let clusteredFeatures = feature.get("features");
  // console.log('feature style', clusteredFeatures);
  if (clusteredFeatures == null || clusteredFeatures.length == 0)
    return [
      new Style({
        stroke: new Stroke({
          color: "#ab34c4",
          width: 2,
          lineDash: [5, 5],
        }),
        image: new Circle({
          radius: 5,
          stroke: new Stroke({
            color: "#ab34c4",
            width: 1,
          }),
          fill: new Fill({
            color: "#ab34c444",
          }),
        }),
      }),
    ];
  const icon = new Icon({
    src: '/cos/' + getPhotoThumb(clusteredFeatures[0].values_.photo),
    width: 100,
    height: 100
  });
  // console.log(icon);
  return new Style({image: icon});

};

const overrideStyleFunction = (feature, style) => {
  const clusteredFeatures = feature.get("features");
  const size = clusteredFeatures.length;

  const icon = new Icon({
    src: '/cos/' + getPhotoThumb(clusteredFeatures[0].values_.photo),
    width: 100,
    height: 100
  });
  style.setImage(icon);
  style.getText()?.setText(size.toString());
};


const featureSelected = (event) => {
  // console.log(event);
  if (event.selected.length == 0 || event.deselected.length == 0) return;
  let clusteredFeatures = event.selected[0].get("features");
  // console.log(clusteredFeatures[0].get("photo"));
  window.open('/cos/origin/' + clusteredFeatures[0].get("photo").name, '_blank');
};

</script>

<script lang="ts">

export default {
  name: 'PhotoMap',
};
</script>

