<template>
  <div class="aui-image">
    <div class="aui-image-text">
      <div class="aui-brand">
        <h2 class="aui-brand-title">气象科研管理平台</h2>
        <p class="aui-brand-desc">
          以数据驱动气象科研创新<br />
          智慧赋能防灾减灾与科学决策
        </p>
        <ul class="aui-brand-list">
          <li><i class="aui-brand-dot"></i>科研数据统一管理</li>
          <li><i class="aui-brand-dot"></i>业务流程高效协同</li>
          <li><i class="aui-brand-dot"></i>智能分析科学决策</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
  .aui-brand {
    position: relative;
    z-index: 1;
    color: #fff;
  }

  .aui-brand-title {
    font-size: 32px;
    font-weight: 600;
    letter-spacing: 2px;
    line-height: 1.4;
    margin-bottom: 12px;
  }

  .aui-brand-desc {
    font-size: 15px;
    line-height: 1.8;
    color: rgba(255, 255, 255, 0.82);
    margin-bottom: 24px;
  }

  .aui-brand-list {
    padding: 0;
    margin: 0;

    li {
      position: relative;
      padding-left: 18px;
      margin-bottom: 10px;
      font-size: 14px;
      color: rgba(255, 255, 255, 0.92);
    }
  }

  .aui-brand-dot {
    position: absolute;
    left: 0;
    top: 7px;
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4dc3ff, #2ee6d6);
    box-shadow: 0 0 8px rgba(78, 195, 255, 0.8);
  }
</style>
