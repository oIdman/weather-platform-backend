<template>
  <div class="node-insertion">
    <a-popover v-model:open="open" trigger="click" placement="right">
      <template #content>
        <div class="node-palette">
          <button v-for="item in items" :key="item.type" @click="add(item.type)">
            <span :style="{ color: item.color }"><component :is="item.icon" /></span>{{ item.name }}
          </button>
        </div>
      </template>
      <button class="insert-button" aria-label="添加流程节点"><PlusOutlined /></button>
    </a-popover>
  </div>
</template>
<script setup lang="ts">
  import { ref } from 'vue';
  import { PlusOutlined, UserOutlined, FormOutlined, SendOutlined, ApartmentOutlined, BranchesOutlined, ClockCircleOutlined, ApiOutlined, PartitionOutlined, GatewayOutlined, ForkOutlined } from '@ant-design/icons-vue';
  const emit = defineEmits<{ add: [type: number] }>();
  const open = ref(false);
  const items = [
    { type: 11, name: '审批人', color: '#ff8c36', icon: UserOutlined },
    { type: 13, name: '办理人', color: '#7544dc', icon: FormOutlined },
    { type: 12, name: '抄送', color: '#328cff', icon: SendOutlined },
    { type: 51, name: '条件分支', color: '#74ba37', icon: ApartmentOutlined },
    { type: 52, name: '并行分支', color: '#7967ff', icon: PartitionOutlined },
    { type: 53, name: '包容分支', color: '#366da9', icon: GatewayOutlined },
    { type: 14, name: '延迟器', color: '#fc636d', icon: ClockCircleOutlined },
    { type: 54, name: '路由分支', color: '#f13e4c', icon: BranchesOutlined },
    { type: 15, name: '触发器', color: '#4089ef', icon: ApiOutlined },
    { type: 20, name: '子流程', color: '#b4793f', icon: ForkOutlined },
  ];
  function add(type: number) { open.value = false; emit('add', type); }
</script>
<style scoped lang="less">
  .node-insertion { display: flex; height: 76px; align-items: center; justify-content: center; background: linear-gradient(#dcdfe6, #dcdfe6) center / 2px 100% no-repeat; }
  .insert-button { display: grid; width: 32px; height: 32px; place-items: center; border: 0; border-radius: 50%; background: #168dff; color: white; font-size: 20px; cursor: pointer; }
  .node-palette { display: grid; grid-template-columns: repeat(4, 90px); gap: 18px 10px; padding: 16px; }
  .node-palette button { display: flex; flex-direction: column; align-items: center; gap: 8px; border: 0; background: white; color: #475467; cursor: pointer; }
  .node-palette button span { display: grid; width: 60px; height: 60px; place-items: center; border: 1px solid #e5e7eb; border-radius: 50%; font-size: 28px; }
  .node-palette button:hover span { border-color: #168dff; background: #f4f9ff; }
</style>
