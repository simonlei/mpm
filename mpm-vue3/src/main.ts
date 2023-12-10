import {createApp} from 'vue';

import TDesign from 'tdesign-vue-next';
import 'tdesign-vue-next/es/style/index.css';
import VueVirtualScroller from 'vue-virtual-scroller';
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css';
import customVideo from 'vue-video-xg';
import "vue-video-xg/lib/style.css"; //引入样式
import {store} from './store';
import router from './router';
import '@/style/index.less';
import './permission';
import App from './App.vue';

const app = createApp(App);

app.use(TDesign);
app.use(store);
app.use(router);
app.use(VueVirtualScroller);
app.use(customVideo);

app.mount('#app');
