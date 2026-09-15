import PaletteProvider from 'bpmn-js/lib/features/palette/PaletteProvider';

type PaletteAction = {
  group: string;
  className?: string;
  title: string;
  separator?: boolean;
  action?: Record<string, (event: Event) => void>;
};

const defaultNodeNames: Record<string, string> = {
  'bpmn:StartEvent': '开始',
  'bpmn:IntermediateCatchEvent': '中间事件',
  'bpmn:IntermediateThrowEvent': '中间事件',
  'bpmn:BoundaryEvent': '边界事件',
  'bpmn:EndEvent': '结束',
  'bpmn:ExclusiveGateway': '排他网关',
  'bpmn:ParallelGateway': '并行网关',
  'bpmn:InclusiveGateway': '包容网关',
  'bpmn:EventBasedGateway': '事件网关',
  'bpmn:UserTask': '审批人',
  'bpmn:Task': '任务',
  'bpmn:ServiceTask': '服务任务',
  'bpmn:ScriptTask': '脚本任务',
  'bpmn:ReceiveTask': '接收任务',
  'bpmn:CallActivity': '调用子流程',
  'bpmn:SubProcess': '扩展子流程',
  'bpmn:DataObjectReference': '数据对象',
  'bpmn:DataStoreReference': '数据存储',
  'bpmn:Group': '分组',
};

export function defaultNodeName(type?: string): string {
  if (!type) return '';
  return defaultNodeNames[type] || '';
}

/**
 * BPMN palette used by the Jeecg workflow designer.
 *
 * bpmn-js 18 deliberately keeps its stock palette small (Task instead of the
 * Flowable task types).  Registering this provider at a higher priority lets
 * us keep the stock tools while exposing the Flowable nodes that the backend
 * can execute: service task, call activity and expanded sub-process.
 */
function BpmnPalette(
  palette: any,
  create: any,
  elementFactory: any,
  spaceTool: any,
  lassoTool: any,
  handTool: any,
  globalConnect: any,
  translate: (value: string) => string,
) {
  this._create = create;
  this._elementFactory = elementFactory;
  this._spaceTool = spaceTool;
  this._lassoTool = lassoTool;
  this._handTool = handTool;
  this._globalConnect = globalConnect;
  this._translate = translate;

  // Override the default provider entries with the same IDs at a higher
  // priority.  This keeps bpmn-js' native drag/click behavior intact.
  palette.registerProvider(2000, this);
}

BpmnPalette.$inject = [
  'palette',
  'create',
  'elementFactory',
  'spaceTool',
  'lassoTool',
  'handTool',
  'globalConnect',
  'translate',
];

BpmnPalette.prototype.getPaletteEntries = function getPaletteEntries(): Record<string, PaletteAction> {
  const create = this._create;
  const elementFactory = this._elementFactory;
  const translate = this._translate;

  const createAction = (type: string, group: string, className: string, title: string, options: Record<string, unknown> = {}) => ({
    group,
    className,
    title,
    action: {
      dragstart: (event: Event) => create.start(event, elementFactory.createShape({
        type,
        name: defaultNodeNames[type],
        ...options,
      })),
      click: (event: Event) => create.start(event, elementFactory.createShape({
        type,
        name: defaultNodeNames[type],
        ...options,
      })),
    },
  });

  const createExpandedSubProcess = (event: Event) => {
    const subProcess = elementFactory.createShape({ type: 'bpmn:SubProcess', name: defaultNodeNames['bpmn:SubProcess'], x: 0, y: 0, isExpanded: true });
    const startEvent = elementFactory.createShape({ type: 'bpmn:StartEvent', name: '子流程开始', x: 40, y: 82, parent: subProcess });
    create.start(event, [subProcess, startEvent], { hints: { autoSelect: [startEvent] } });
  };

  return {
    'hand-tool': {
      group: 'tools',
      className: 'bpmn-icon-hand-tool',
      title: translate('激活手形工具'),
      action: { click: (event: Event) => this._handTool.activateHand(event) },
    },
    'lasso-tool': {
      group: 'tools',
      className: 'bpmn-icon-lasso-tool',
      title: translate('框选工具'),
      action: { click: (event: Event) => this._lassoTool.activateSelection(event) },
    },
    'space-tool': {
      group: 'tools',
      className: 'bpmn-icon-space-tool',
      title: translate('激活创建/删除空间工具'),
      action: { click: (event: Event) => this._spaceTool.activateSelection(event) },
    },
    'global-connect-tool': {
      group: 'tools',
      className: 'bpmn-icon-connection-multi',
      title: translate('激活全局连线工具'),
      action: { click: (event: Event) => this._globalConnect.toggle(event) },
    },
    'tool-separator': { group: 'tools', separator: true, title: '' },
    'create.start-event': createAction('bpmn:StartEvent', 'event', 'bpmn-icon-start-event-none', '创建开始事件'),
    'create.intermediate-event': createAction('bpmn:IntermediateThrowEvent', 'event', 'bpmn-icon-intermediate-event-none', '创建中间/边界事件'),
    'create.end-event': createAction('bpmn:EndEvent', 'event', 'bpmn-icon-end-event-none', '创建结束事件'),
    'create.exclusive-gateway': createAction('bpmn:ExclusiveGateway', 'gateway', 'bpmn-icon-gateway-none', '创建排他网关'),
    'create.parallel-gateway': createAction('bpmn:ParallelGateway', 'gateway', 'bpmn-icon-gateway-parallel', '创建并行网关'),
    'create.inclusive-gateway': createAction('bpmn:InclusiveGateway', 'gateway', 'bpmn-icon-gateway-or', '创建包容网关'),
    'create.event-based-gateway': createAction('bpmn:EventBasedGateway', 'gateway', 'bpmn-icon-gateway-eventbased', '创建事件网关'),
    'create.user-task': createAction('bpmn:UserTask', 'activity', 'bpmn-icon-user-task', '创建用户任务'),
    'create.service-task': createAction('bpmn:ServiceTask', 'activity', 'bpmn-icon-service', '创建服务任务'),
    'create.script-task': createAction('bpmn:ScriptTask', 'activity', 'bpmn-icon-script-task', '创建脚本任务'),
    'create.receive-task': createAction('bpmn:ReceiveTask', 'activity', 'bpmn-icon-receive-task', '创建接收任务'),
    'create.call-activity': createAction('bpmn:CallActivity', 'activity', 'bpmn-icon-call-activity', '创建调用子流程'),
    'create.subprocess-expanded': {
      group: 'activity',
      className: 'bpmn-icon-subprocess-expanded',
      title: '创建扩展子流程',
      action: { dragstart: createExpandedSubProcess, click: createExpandedSubProcess },
    },
    'create.data-object': createAction('bpmn:DataObjectReference', 'data-object', 'bpmn-icon-data-object', '创建数据对象'),
    'create.data-store': createAction('bpmn:DataStoreReference', 'data-store', 'bpmn-icon-data-store', '创建数据存储'),
    'create.group': createAction('bpmn:Group', 'artifact', 'bpmn-icon-group', '创建分组'),
  };
};

export default {
  __init__: ['bpmnPalette'],
  bpmnPalette: ['type', BpmnPalette],
};
