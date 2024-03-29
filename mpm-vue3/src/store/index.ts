import {createPinia} from 'pinia';
import {createPersistedState} from 'pinia-plugin-persistedstate';

const store = createPinia();
store.use(createPersistedState());

export {store};

export * from './modules/notification';
export * from './modules/permission';
export * from './modules/user';
export * from './modules/setting';
export * from './modules/tabs-router';
export * from './modules/photo-filter';
export * from './modules/photo-module';
export * from './modules/detail-view-module';
export * from './modules/dialogs';
export default store;
