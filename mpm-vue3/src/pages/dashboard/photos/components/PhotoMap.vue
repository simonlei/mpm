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
      :pointRadius="20"
      @select="featureSelected"
    >
      <ol-style :overrideStyleFunction="overrideStyleFunction">
      </ol-style>
    </ol-interaction-clusterselect>

    <ol-animated-clusterlayer :animationDuration="500" :distance="40">
      <ol-source-vector ref="vectorsource" :features="features">
      </ol-source-vector>

      <ol-style :overrideStyleFunction="overrideStyleFunction">
        <ol-style-stroke :width="2" color="red"></ol-style-stroke>
        <ol-style-fill color="rgba(255,255,255,0.1)"></ol-style-fill>

        <ol-style-circle :radius="20">
          <ol-style-stroke
            :lineDash="[]"
            :width="15"
            color="black"
            lineCap="butt"
          ></ol-style-stroke>
          <ol-style-fill color="black"></ol-style-fill>
        </ol-style-circle>

        <ol-style-text>
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

const center = ref([107, 31]);
const projection = ref("EPSG:4326");
const zoom = ref(5);
const rotation = ref(0);
let markers = await loadMarkers();

const features = computed(() => {

  return markers.map((photo, index) => {
    return new Feature({
      geometry: new Point([photo.longitude, photo.latitude]),
      photo: photo,
    });
  });
});

// style of the "artificial" item markers and lines connected to the cluster base after first click on the cluster -->
const featureStyle = () => {
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
};

const overrideStyleFunction = (feature, style) => {
  const clusteredFeatures = feature.get("features");
  const size = clusteredFeatures.length;

  const color = size > 20 ? "192,0,0" : size > 8 ? "255,128,0" : "0,128,0";
  const radius = Math.max(8, Math.min(size, 20));
  const dash = (2 * Math.PI * radius) / 6;
  const calculatedDash = [0, dash, dash, dash, dash, dash, dash];
  // console.log(clusteredFeatures[0].values_.photo);

  const icon = new Icon({
    src: '/cos/' + clusteredFeatures[0].values_.photo?.thumb,
    width: 100,
    height: 100
  });
  console.log(icon);
  style.setImage(icon);

  /*

  style.getImage().src = '/cos/' + clusteredFeatures[0].thumb;
  style.getImage().getStroke().setLineDash(dash);
  style.getImage().getStroke().setColor("rgba(" + color + ",0.5)");
  style.getImage().getStroke().setLineDash(calculatedDash);
  style.getImage().getFill().setColor("rgba(" + color + ",1)");

  style.getImage().setRadius(radius);
  // style.setImageStyle(new Icon({src: markerIcon}))

   */
  style.getText().setText(size.toString());
};


const featureSelected = (event) => {
  console.log(event);
};

</script>

<script lang="ts">

export default {
  name: 'PhotoMap',
};
</script>

