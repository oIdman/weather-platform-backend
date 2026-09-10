export default {
  name: 'Flowable',
  uri: 'http://flowable.org/bpmn',
  prefix: 'flowable',
  xml: { tagAlias: 'lowerCase' },
  associations: [],
  types: [
    {
      name: 'Assignable',
      extends: ['bpmn:UserTask'],
      properties: [
        { name: 'assignee', isAttr: true, type: 'String' },
        { name: 'candidateUsers', isAttr: true, type: 'String' },
        { name: 'candidateGroups', isAttr: true, type: 'String' },
        { name: 'dueDate', isAttr: true, type: 'String' },
        { name: 'priority', isAttr: true, type: 'String' },
      ],
    },
    {
      name: 'FormSupported',
      isAbstract: true,
      extends: ['bpmn:StartEvent', 'bpmn:UserTask'],
      properties: [{ name: 'formKey', isAttr: true, type: 'String' }],
    },
    {
      name: 'ServiceTaskLike',
      extends: ['bpmn:ServiceTask', 'bpmn:BusinessRuleTask', 'bpmn:SendTask'],
      properties: [
        { name: 'expression', isAttr: true, type: 'String' },
        { name: 'class', isAttr: true, type: 'String' },
        { name: 'delegateExpression', isAttr: true, type: 'String' },
        { name: 'resultVariable', isAttr: true, type: 'String' },
      ],
    },
    {
      name: 'AsyncCapable',
      isAbstract: true,
      extends: ['bpmn:Activity', 'bpmn:Gateway', 'bpmn:Event'],
      properties: [
        { name: 'async', isAttr: true, type: 'Boolean', default: false },
        { name: 'exclusive', isAttr: true, type: 'Boolean', default: true },
      ],
    },
  ],
  enumerations: [],
};
