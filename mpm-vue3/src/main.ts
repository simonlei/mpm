import {createApp} from 'vue';

import TDesign from 'tdesign-vue-next';
import 'tdesign-vue-next/es/style/index.css';
import VueVirtualScroller from 'vue-virtual-scroller';
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css';
import {store} from './store';
import router from './router';
import '@/style/index.less';
import './permission';
import App from './App.vue';
import vue3videoPlay from "vue3-video-play-fix"; // 引入组件
import "vue3-video-play-fix/dist/style.css";
import ContextMenu from "@imengyu/vue3-context-menu"; // 引入css
import '@imengyu/vue3-context-menu/lib/vue3-context-menu.css'
import OpenLayersMap from "vue3-openlayers";

const app = createApp(App);

app.use(TDesign);
app.use(store);
app.use(router);
app.use(VueVirtualScroller);
app.use(vue3videoPlay);
app.use(ContextMenu);
app.use(OpenLayersMap);


app.mount('#app');
