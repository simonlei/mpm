import {ConfigEnv, loadEnv, UserConfig} from 'vite';
import {viteMockServe} from 'vite-plugin-mock';
import createVuePlugin from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import svgLoader from 'vite-svg-loader';

import path from 'path';

const CWD = process.cwd();

// https://vitejs.dev/config/
export default ({mode}: ConfigEnv): UserConfig => {
  const {VITE_BASE_URL} = loadEnv(mode, CWD);
  return {
    define: {
      __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: true,
    },
    base: VITE_BASE_URL,
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },

    css: {
      preprocessorOptions: {
        less: {
          modifyVars: {
            hack: `true; @import (reference) "${path.resolve('src/style/variables.less')}";`,
          },
          math: 'strict',
          javascriptEnabled: true,
        },
      },
    },

    plugins: [
      createVuePlugin(),
      vueJsx(),
      viteMockServe({
        mockPath: 'mock',
        enable: false,
      }),
      svgLoader(),
    ],

    server: {
      port: 3002,
      host: '0.0.0.0',
      proxy: {
        '/api': 'http://127.0.0.1:18880/',
        '/geo_json_api': 'http://127.0.0.1:18880/',
        '/get_face_img': 'http://127.0.0.1:18880/',
        '/cos': 'http://127.0.0.1:18880/',
      },
    },
  };
};
