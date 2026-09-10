import type { App } from 'vue';

import formCreate from '@form-create/ant-design-vue';
import install from '@form-create/ant-design-vue/auto-import';
import FcDesigner from '@form-create/antd-designer';
import {
  Alert,
  Badge,
  Card,
  Collapse,
  CollapsePanel,
  ConfigProvider,
  Divider,
  Dropdown,
  Image,
  Layout,
  LayoutContent,
  LayoutFooter,
  LayoutHeader,
  LayoutSider,
  Menu,
  MenuDivider,
  MenuItem,
  Popconfirm,
  Table,
  TableColumn,
  TabPane,
  Tabs,
  Tag,
  Transfer,
} from 'ant-design-vue';

/**
 * form-create/designer is distributed as a pre-compiled Vue library. Its
 * internal templates are not processed by this application's component auto
 * importer, so the Ant Design components used by those templates must be
 * registered on the root app explicitly.
 */
const components = [
  Alert,
  Badge,
  Card,
  Collapse,
  CollapsePanel,
  ConfigProvider,
  Divider,
  Dropdown,
  Image,
  Layout,
  LayoutContent,
  LayoutFooter,
  LayoutHeader,
  LayoutSider,
  Menu,
  MenuDivider,
  MenuItem,
  Popconfirm,
  Table,
  TableColumn,
  TabPane,
  Tabs,
  Tag,
  Transfer,
];

export function setupFormCreate(app: App) {
  components.forEach((component) => {
    app.component(component.name as string, component);
  });

  // Register form-create's own Ant Design aliases (AForm, AInput, ARow, ...)
  // and the FcDesigner component used by the BPM form editor.
  formCreate.use(install);
  app.use(formCreate);
  app.use(FcDesigner);
}
