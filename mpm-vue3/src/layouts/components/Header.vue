<template>
  <div :class="layoutCls">
    <t-head-menu :class="menuCls" :theme="'dark'" :value="active" expand-type="popup">
      <template v-if="layout !== 'side'" #default>
        <menu-content :nav-data="menu" class="header-menu" />
      </template>
      <!-- My Photo Manager(1111) [ ] 跳转 []只看星标 []只看视频 [tags dropdown] [排序（c] 回收站(1) 地图模式 上传照片 -->
      <template #operations>
        <div class="operations-container">

          <t-tooltip content="跳转至指定位置，按回车确定" placement="bottom">
            <t-input-number v-model:value="selectStore.lastSelectedIndex" :label="'跳转至'" :onEnter="jumpTo"
              align="center" theme="normal" />
          </t-tooltip>
          <t-dropdown trigger="click">
            <template #dropdown>
              <t-dropdown-menu>
                <t-dropdown-item class="operations-dropdown-container-item">
                  <template #content>
                    <video-only-checkbox />
                  </template>
                </t-dropdown-item>
                <t-dropdown-item class="operations-dropdown-container-item">
                  <template #content>
                    <star-only-checkbox />
                  </template>
                </t-dropdown-item>
                <t-dropdown-item class="operations-dropdown-container-item">
                  <template #content>
                    <Suspense>
                      <tag-filter-select />
                    </Suspense>
                  </template>
                </t-dropdown-item>
              </t-dropdown-menu>
            </template>
            <t-button class="header-user-btn" theme="default" variant="text">
              <template #icon>
                <t-icon class="header-user-avatar" name="filter" />
              </template>
              <template #suffix>
                <t-icon name="chevron-down" />
              </template>
            </t-button>
          </t-dropdown>

          <t-dropdown trigger="click">
            <template #dropdown>
              <t-dropdown-menu :maxColumnWidth="150">
                <t-dropdown-item class="operations-dropdown-container-item">
                  <template #content>
                    <order-selector />
                  </template>
                </t-dropdown-item>
              </t-dropdown-menu>
            </template>
            <t-button class="header-user-btn" theme="default" variant="text">
              <template #icon>
                <t-icon class="header-user-avatar" name="order-descending" />
              </template>
              <template #suffix>
                <t-icon name="chevron-down" />
              </template>
            </t-button>
          </t-dropdown>
          <key-board-listener />


          <switch-trash-button />
          <empty-trash-button />
          <!-- 搜索框 -->
          <search v-if="layout !== 'side'" :layout="layout" />

          <upload-photo-button />

          <t-tooltip content="代码仓库" placement="bottom">
            <t-button shape="square" theme="default" variant="text" @click="navToGitHub">
              <t-icon name="logo-github" />
            </t-button>
          </t-tooltip>
          <t-dropdown trigger="click">
            <template #dropdown>
              <t-dropdown-menu>
                <t-dropdown-item class="operations-dropdown-container-item" @click="handleNav('/user/index')">
                  <t-icon name="user-circle"></t-icon>
                  个人中心
                </t-dropdown-item>
                <t-dropdown-item class="operations-dropdown-container-item" @click="handleTotp">
                  <t-icon name="https"></t-icon>
                  Totp
                </t-dropdown-item>
                <t-dropdown-item class="operations-dropdown-container-item" @click="handleLogout">
                  <t-icon name="poweroff"></t-icon>
                  退出登录
                </t-dropdown-item>
              </t-dropdown-menu>
            </template>
            <t-button class="header-user-btn" theme="default" variant="text">
              <template #icon>
                <t-icon class="header-user-avatar" name="user-circle" />
              </template>
              <template #suffix>
                <t-icon name="chevron-down" />
              </template>
            </t-button>
          </t-dropdown>
          <!--
                    <t-tooltip content="系统设置" placement="bottom">
                      <t-button shape="square" theme="default" variant="text" @click="toggleSettingPanel">
                        <t-icon name="setting"/>
                      </t-button>
                    </t-tooltip>
          -->
        </div>
      </template>
    </t-head-menu>
  </div>
</template>

<script lang="ts" setup>
import type { PropType } from 'vue';
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useSettingStore } from '@/store';
import { getActive } from '@/router';
import { prefix } from '@/config/global';
import type { MenuRoute } from '@/types/interface';

import Search from './Search.vue';
import MenuContent from './MenuContent.vue';
import { selectModuleStore } from "@/store/modules/select-module";
import SwitchTrashButton from "@/layouts/components/SwitchTrashButton.vue";
import UploadPhotoButton from "@/layouts/components/UploadPhotoButton.vue";
import OrderSelector from "@/layouts/components/OrderSelector.vue";
import EmptyTrashButton from "@/layouts/components/EmptyTrashButton.vue";
import VideoOnlyCheckbox from "@/layouts/components/VideoOnlyCheckbox.vue";
import StarOnlyCheckbox from "@/layouts/components/StarOnlyCheckbox.vue";
import KeyBoardListener from "@/pages/dashboard/photos/components/KeyBoardListener.vue";
import TagFilterSelect from "@/layouts/components/TagFilterSelect.vue";

import { totp } from "@/api/users"

const props = defineProps({
  theme: {
    type: String,
    default: '',
  },
  layout: {
    type: String,
    default: 'top',
  },
  showLogo: {
    type: Boolean,
    default: true,
  },
  menu: {
    type: Array as PropType<MenuRoute[]>,
    default: () => [],
  },
  isFixed: {
    type: Boolean,
    default: false,
  },
  isCompact: {
    type: Boolean,
    default: false,
  },
  maxLevel: {
    type: Number,
    default: 3,
  },
});

const router = useRouter();
const settingStore = useSettingStore();
const selectStore = selectModuleStore();

const toggleSettingPanel = () => {
  settingStore.updateConfig({
    showSettingPanel: true,
  });
};

const active = computed(() => getActive());

const layoutCls = computed(() => [`${prefix}-header-layout`]);

const menuCls = computed(() => {
  const { isFixed, layout, isCompact } = props;
  return [
    {
      [`${prefix}-header-menu`]: !isFixed,
      [`${prefix}-header-menu-fixed`]: isFixed,
      [`${prefix}-header-menu-fixed-side`]: layout === 'side' && isFixed,
      [`${prefix}-header-menu-fixed-side-compact`]: layout === 'side' && isFixed && isCompact,
    },
  ];
});

const handleNav = (url) => {
  router.push(url);
};

const handleTotp = () => {
  totp().then((resp) => alert("TOTP:" + resp)).catch((error) => {
    console.error('请求 TOTP 接口出错:', error);
    // 若请求出错，弹出错误信息
    alert('请求 TOTP 接口出错，请稍后重试');
  });
}

const handleLogout = () => {
  router.push({
    path: '/login',
    query: { redirect: encodeURIComponent(router.currentRoute.value.fullPath) },
  });
};

const navToGitHub = () => {
  window.open('https://github.com/simonlei/mpm');
};

const jumpTo = (value: number, context) => {
  selectStore.selectIndex(value, false, false);
  context.e.target.blur();
  context.e.stopPropagation();
};
</script>
<style lang="less" scoped>
.tdesign-starter-header {
  &-menu-fixed {
    position: fixed;
    top: 0;
    z-index: 1001;

    &-side {
      left: 232px;
      right: 0;
      z-index: 10;
      width: auto;
      transition: all 0.3s;

      &-compact {
        left: 64px;
      }
    }
  }

  &-logo-container {
    cursor: pointer;
    display: inline-flex;
  }
}

.header-menu {
  flex: 1 1 1;
  display: inline-flex;

  :deep(.t-menu__item) {
    min-width: unset;
    padding: 0px 16px;
  }
}

.operations-container {
  display: flex;
  align-items: center;
  margin-right: 12px;

  .t-popup__reference {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .t-button {
    margin: 0 8px;

    &.header-user-btn {
      margin: 0;
    }
  }

  .t-icon {
    font-size: 20px;

    &.general {
      margin-right: 16px;
    }
  }
}

.header-operate-left {
  display: flex;
  margin-left: 20px;
  align-items: normal;
  line-height: 0;

  .collapsed-icon {
    font-size: 20px;
  }
}

.header-logo-container {
  width: 184px;
  height: 26px;
  display: flex;
  margin-left: 24px;
  color: var(--td-text-color-primary);

  .t-logo {
    width: 100%;
    height: 100%;

    &:hover {
      cursor: pointer;
    }
  }

  &:hover {
    cursor: pointer;
  }
}

.header-user-account {
  display: inline-flex;
  align-items: center;
  color: var(--td-text-color-primary);

  .t-icon {
    margin-left: 4px;
    font-size: 16px;
  }
}

:deep(.t-head-menu__inner) {
  border-bottom: 1px solid var(--td-border-level-1-color);
}

.t-menu--light {
  .header-user-account {
    color: var(--td-text-color-primary);
  }
}

.t-menu--dark {
  .t-head-menu__inner {
    border-bottom: 1px solid var(--td-gray-color-10);
  }

  .header-user-account {
    color: rgba(255, 255, 255, 0.55);
  }

  .t-button {
    --ripple-color: var(--td-gray-color-10) !important;

    &:hover {
      background: var(--td-gray-color-12) !important;
    }
  }
}

.operations-dropdown-container-item {
  width: 100%;
  display: flex;
  align-items: center;

  .t-icon {
    margin-right: 8px;
  }

  :deep(.t-dropdown__item) {
    .t-dropdown__item__content {
      display: flex;
      justify-content: center;
    }

    .t-dropdown__item__content__text {
      display: flex;
      align-items: center;
      font-size: 14px;
    }
  }

  :deep(.t-dropdown__item) {
    width: 100%;
    margin-bottom: 0px;
  }

  &:last-child {
    :deep(.t-dropdown__item) {
      margin-bottom: 8px;
    }
  }
}
</style>
