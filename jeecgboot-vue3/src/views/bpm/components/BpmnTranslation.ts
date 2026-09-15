type ReplacementMap = Record<string, unknown>;

const translations: Record<string, string> = {
  Task: '任务',
  'User task': '用户任务',
  'Service task': '服务任务',
  'Send task': '发送任务',
  'Receive task': '接收任务',
  'Manual task': '手工任务',
  'Business rule task': '业务规则任务',
  'Script task': '脚本任务',
  'Call activity': '调用子流程',
  Transaction: '事务子流程',
  'Event sub-process': '事件子流程',
  'Sub-process (collapsed)': '折叠子流程',
  'Sub-process (expanded)': '扩展子流程',
  'Ad-hoc sub-process (collapsed)': '折叠临时子流程',
  'Ad-hoc sub-process (expanded)': '扩展临时子流程',
  'Exclusive gateway': '排他网关',
  'Parallel gateway': '并行网关',
  'Inclusive gateway': '包容网关',
  'Complex gateway': '复杂网关',
  'Event-based gateway': '事件网关',
  'Data store reference': '数据存储',
  'Data object reference': '数据对象',
  'Expanded pool/participant': '展开的泳池/参与者',
  'Empty pool/participant': '空泳池/参与者',
  'Start event': '开始事件',
  'Intermediate throw event': '中间抛出事件',
  'Intermediate catch event': '中间捕获事件',
  'Boundary event': '边界事件',
  'End event': '结束事件',
  'Message start event': '消息开始事件',
  'Message start event (non-interrupting)': '非中断消息开始事件',
  'Timer start event': '定时开始事件',
  'Timer start event (non-interrupting)': '非中断定时开始事件',
  'Conditional start event': '条件开始事件',
  'Conditional start event (non-interrupting)': '非中断条件开始事件',
  'Signal start event': '信号开始事件',
  'Signal start event (non-interrupting)': '非中断信号开始事件',
  'Error start event': '错误开始事件',
  'Message intermediate catch event': '消息中间捕获事件',
  'Message intermediate throw event': '消息中间抛出事件',
  'Timer intermediate catch event': '定时中间捕获事件',
  'Conditional intermediate catch event': '条件中间捕获事件',
  'Signal intermediate catch event': '信号中间捕获事件',
  'Signal intermediate throw event': '信号中间抛出事件',
  'Message boundary event': '消息边界事件',
  'Message boundary event (non-interrupting)': '非中断消息边界事件',
  'Timer boundary event': '定时边界事件',
  'Timer boundary event (non-interrupting)': '非中断定时边界事件',
  'Conditional boundary event': '条件边界事件',
  'Conditional boundary event (non-interrupting)': '非中断条件边界事件',
  'Error boundary event': '错误边界事件',
  'Signal boundary event': '信号边界事件',
  'Signal boundary event (non-interrupting)': '非中断信号边界事件',
  'Cancel boundary event': '取消边界事件',
  'Cancel end event': '取消结束事件',
  'Compensation boundary event': '补偿边界事件',
  'Compensation end event': '补偿结束事件',
  'Compensation intermediate throw event': '补偿中间抛出事件',
  'Compensation start event': '补偿开始事件',
  'Escalation boundary event': '升级边界事件',
  'Escalation boundary event (non-interrupting)': '非中断升级边界事件',
  'Escalation end event': '升级结束事件',
  'Escalation intermediate throw event': '升级中间抛出事件',
  'Escalation start event': '升级开始事件',
  'Escalation start event (non-interrupting)': '非中断升级开始事件',
  'Link intermediate catch event': '链接中间捕获事件',
  'Link intermediate throw event': '链接中间抛出事件',
  'Message end event': '消息结束事件',
  'Error end event': '错误结束事件',
  'Signal end event': '信号结束事件',
  'Terminate end event': '终止结束事件',
  'Sequence flow': '顺序流',
  'Default flow': '默认流',
  'Conditional flow': '条件流',
  Delete: '删除',
  'Connect using association': '使用关联连线',
  'Connect using sequence flow': '使用顺序流',
  'Connect using message flow': '使用消息流',
  'Connect using data input association': '使用数据输入关联',
  'Connect using data output association': '使用数据输出关联',
  'Change type': '更改类型',
  Remove: '移除',
  'Create task': '创建任务',
  'Create user task': '创建用户任务',
  'Create service task': '创建服务任务',
  'Create gateway': '创建网关',
  'Create data object reference': '创建数据对象',
  'Create data store reference': '创建数据存储',
  'Create pool/participant': '创建泳池/参与者',
  'Append task': '追加任务',
  'Append user task': '追加用户任务',
  'Append service task': '追加服务任务',
  'Append gateway': '追加网关',
  'Append end event': '追加结束事件',
};

const normalizedTranslations = Object.fromEntries(
  Object.entries(translations).map(([key, value]) => [key.toLocaleLowerCase(), value]),
);

/** Translate a bpmn-js default label while leaving user-defined names untouched. */
export function translateBpmnLabel(template: string): string {
  const value = String(template || '').trim();
  return translations[value] || normalizedTranslations[value.toLocaleLowerCase()] || '';
}

function customTranslate(template: string, replacements?: ReplacementMap): string {
  const translated = translateBpmnLabel(template) || template;
  return translated.replace(/{([^}]+)}/g, (_match, key: string) => {
    const replacement = replacements?.[key];
    return replacement === undefined ? `{${key}}` : String(replacement);
  });
}

export default {
  translate: ['value', customTranslate],
};
