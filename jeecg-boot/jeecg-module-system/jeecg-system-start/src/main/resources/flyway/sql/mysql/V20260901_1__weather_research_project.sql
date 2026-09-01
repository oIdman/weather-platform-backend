-- 气象科研管理平台业务模块迁移（JeecgBoot 3.1.0 -> 3.9.5）
-- 原始脚本按 Phase 0-7 顺序合并；仅适用于 MySQL。

-- ---------------------------------------------------------------------
-- Source: 2026-08-21_mrp_phase0_scaffold.sql
-- ---------------------------------------------------------------------
-- =====================================================================
-- 气象科研管理平台 · Phase 0 基础脚手架 DDL
-- 日期: 2026-08-21
-- 说明: 科研人员库 / 专家库 / 公共附件表（最小化脚手架，保证核心闭环跑通）
-- 规范: 表前缀 mrp_，主键 id varchar(36)（ASSIGN_ID），统一审计字段 + 逻辑删除
-- 注意: 字典数据（成果类型、附件业务类型等）随各业务 Phase 初始化 sys_dict，本脚本不含
-- =====================================================================

-- 科研人员库
CREATE TABLE IF NOT EXISTS `mrp_researcher` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `employee_no` varchar(50) DEFAULT NULL COMMENT '工号',
  `org_name` varchar(100) DEFAULT NULL COMMENT '所属机构',
  `title` varchar(50) DEFAULT NULL COMMENT '职称',
  `research_field` varchar(100) DEFAULT NULL COMMENT '研究方向',
  `ongoing_project_count` int DEFAULT 0 COMMENT '在研项目数',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_researcher_employee_no` (`employee_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科研人员库';

-- 科研人员关联成果（论文/专利/软著/奖励，支撑申报自动带入既往成果）
CREATE TABLE IF NOT EXISTS `mrp_researcher_achievement` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `researcher_id` varchar(36) NOT NULL COMMENT '科研人员ID',
  `achievement_type` varchar(20) DEFAULT NULL COMMENT '成果类型（论文/专利/软著/奖励）',
  `achievement_title` varchar(200) DEFAULT NULL COMMENT '成果名称',
  `publish_date` date DEFAULT NULL COMMENT '成果日期',
  `source` varchar(200) DEFAULT NULL COMMENT '来源（期刊/授权机构等）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_achievement_researcher` (`researcher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科研人员关联成果';

-- 专家库
CREATE TABLE IF NOT EXISTS `mrp_expert` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `org_name` varchar(100) DEFAULT NULL COMMENT '所属机构',
  `research_field` varchar(100) DEFAULT NULL COMMENT '研究领域',
  `star_level` int DEFAULT 3 COMMENT '专家星级(1-5)',
  `avoid_org` varchar(100) DEFAULT NULL COMMENT '回避单位',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_expert_field` (`research_field`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家库';

-- 公共附件表（多态：biz_type + biz_id）
CREATE TABLE IF NOT EXISTS `mrp_attachment` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `biz_type` varchar(20) DEFAULT NULL COMMENT '业务类型（guide/application/review/project/midcheck/acceptance/archive）',
  `biz_id` varchar(36) DEFAULT NULL COMMENT '业务主键',
  `file_name` varchar(200) DEFAULT NULL COMMENT '文件名称',
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件地址',
  `file_type` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `upload_by` varchar(50) DEFAULT NULL COMMENT '上传人',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_attachment_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公共附件表';


-- ---------------------------------------------------------------------
-- Source: 2026-08-22_mrp_dict_init.sql
-- ---------------------------------------------------------------------
-- =====================================================================
-- 气象科研管理平台 · 字典初始化 SQL
-- 日期: 2026-08-22
-- 说明: Phase 0 相关业务字典（成果类型 / 附件业务类型 / 项目类型 / 研究方向 / 变更类型 / 验收等级 / 专家星级）
-- 幂等: 重复执行安全（dict_code 唯一键，冲突时仅更新名称与描述）
-- =====================================================================

-- 成果类型（mrp_researcher_achievement.achievement_type）
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000001', '成果类型', 'mrp_achievement_type', '科研人员关联成果类型', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000000101', '1700000000000000001', '论文', 'paper', '', 1, 1, 'admin', NOW()),
  ('1700000000000000102', '1700000000000000001', '专利', 'patent', '', 2, 1, 'admin', NOW()),
  ('1700000000000000103', '1700000000000000001', '软著', 'software_copyright', '', 3, 1, 'admin', NOW()),
  ('1700000000000000104', '1700000000000000001', '奖励', 'award', '', 4, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 附件业务类型（mrp_attachment.biz_type）
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000002', '附件业务类型', 'mrp_attachment_biz_type', '公共附件表业务类型', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000000201', '1700000000000000002', '课题指南', 'guide', '', 1, 1, 'admin', NOW()),
  ('1700000000000000202', '1700000000000000002', '课题申报', 'application', '', 2, 1, 'admin', NOW()),
  ('1700000000000000203', '1700000000000000002', '申报审查', 'review', '', 3, 1, 'admin', NOW()),
  ('1700000000000000204', '1700000000000000002', '立项项目', 'project', '', 4, 1, 'admin', NOW()),
  ('1700000000000000205', '1700000000000000002', '中期检查', 'midcheck', '', 5, 1, 'admin', NOW()),
  ('1700000000000000206', '1700000000000000002', '验收', 'acceptance', '', 6, 1, 'admin', NOW()),
  ('1700000000000000207', '1700000000000000002', '归档', 'archive', '', 7, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 项目类型
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000003', '项目类型', 'project_type', '科研项目类型', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000000301', '1700000000000000003', '基础研究', 'basic_research', '', 1, 1, 'admin', NOW()),
  ('1700000000000000302', '1700000000000000003', '应用研究', 'applied_research', '', 2, 1, 'admin', NOW()),
  ('1700000000000000303', '1700000000000000003', '技术研发', 'tech_development', '', 3, 1, 'admin', NOW()),
  ('1700000000000000304', '1700000000000000003', '软科学', 'soft_science', '', 4, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 研究方向
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000004', '研究方向', 'research_field', '气象科研研究方向', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000000401', '1700000000000000004', '数值预报', '数值预报', '', 1, 1, 'admin', NOW()),
  ('1700000000000000402', '1700000000000000004', '气候预测', '气候预测', '', 2, 1, 'admin', NOW()),
  ('1700000000000000403', '1700000000000000004', '气候变化', '气候变化', '', 3, 1, 'admin', NOW()),
  ('1700000000000000404', '1700000000000000004', '大气探测', '大气探测', '', 4, 1, 'admin', NOW()),
  ('1700000000000000405', '1700000000000000004', '雷达气象', '雷达气象', '', 5, 1, 'admin', NOW()),
  ('1700000000000000406', '1700000000000000004', '人工影响天气', '人工影响天气', '', 6, 1, 'admin', NOW()),
  ('1700000000000000407', '1700000000000000004', '农业气象', '农业气象', '', 7, 1, 'admin', NOW()),
  ('1700000000000000408', '1700000000000000004', '气象服务', '气象服务', '', 8, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 变更类型
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000005', '变更类型', 'change_type', '项目变更类型', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000000501', '1700000000000000005', '研究方向变更', 'direction_change', '', 1, 1, 'admin', NOW()),
  ('1700000000000000502', '1700000000000000005', '预算变更', 'budget_change', '', 2, 1, 'admin', NOW()),
  ('1700000000000000503', '1700000000000000005', '进度变更', 'schedule_change', '', 3, 1, 'admin', NOW()),
  ('1700000000000000504', '1700000000000000005', '关键人员变更', 'key_person_change', '', 4, 1, 'admin', NOW()),
  ('1700000000000000505', '1700000000000000005', '其他', 'other', '', 5, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 验收等级
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000006', '验收等级', 'acceptance_level', '验收结论等级', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000000601', '1700000000000000006', '优秀', 'excellent', '', 1, 1, 'admin', NOW()),
  ('1700000000000000602', '1700000000000000006', '合格', 'qualified', '', 2, 1, 'admin', NOW()),
  ('1700000000000000603', '1700000000000000006', '不合格', 'unqualified', '', 3, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 专家星级（mrp_expert.star_level，1-5）
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000007', '专家星级', 'expert_star_level', '专家星级(1-5)', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000000701', '1700000000000000007', '1星', '1', '', 1, 1, 'admin', NOW()),
  ('1700000000000000702', '1700000000000000007', '2星', '2', '', 2, 1, 'admin', NOW()),
  ('1700000000000000703', '1700000000000000007', '3星', '3', '', 3, 1, 'admin', NOW()),
  ('1700000000000000704', '1700000000000000007', '4星', '4', '', 4, 1, 'admin', NOW()),
  ('1700000000000000705', '1700000000000000007', '5星', '5', '', 5, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;


-- ---------------------------------------------------------------------
-- Source: 2026-08-22_mrp_phase1_guide.sql
-- ---------------------------------------------------------------------
-- =====================================================================
-- 气象科研管理平台 · Phase 1 课题指南管理 DDL + 字典
-- 日期: 2026-08-22
-- 说明: 指南主表 / 类目 / 模板库 / 验收考核指标 / 解读答疑 / 版本快照
-- 幂等: CREATE TABLE IF NOT EXISTS；字典走唯一键冲突更新
-- =====================================================================

-- 课题指南主表
CREATE TABLE IF NOT EXISTS `mrp_guide` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `guide_title` varchar(200) DEFAULT NULL COMMENT '指南标题',
  `guide_year` varchar(10) DEFAULT NULL COMMENT '指南年度',
  `stage` varchar(50) DEFAULT NULL COMMENT '指南阶段/批次',
  `project_type` varchar(50) DEFAULT NULL COMMENT '项目类型（字典 project_type）',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-草稿,1-待发布,2-已发布,3-已归档)',
  `publish_scope` varchar(20) DEFAULT 'all' COMMENT '发布范围(all-全员,org-按机构)',
  `content` longtext COMMENT '指南正文',
  `publisher` varchar(50) DEFAULT NULL COMMENT '发布人',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `archive_time` datetime DEFAULT NULL COMMENT '归档时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_guide_year` (`guide_year`),
  KEY `idx_mrp_guide_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题指南主表';

-- 指南类目
CREATE TABLE IF NOT EXISTS `mrp_guide_category` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `guide_id` varchar(36) NOT NULL COMMENT '指南ID',
  `parent_id` varchar(36) DEFAULT NULL COMMENT '父类目ID',
  `category_name` varchar(100) DEFAULT NULL COMMENT '类目名称',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_guide_category_guide` (`guide_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指南类目';

-- 指南模板库（年度/阶段模板）
CREATE TABLE IF NOT EXISTS `mrp_guide_template` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `template_name` varchar(100) DEFAULT NULL COMMENT '模板名称',
  `template_year` varchar(10) DEFAULT NULL COMMENT '模板年度',
  `stage` varchar(50) DEFAULT NULL COMMENT '阶段/批次',
  `template_content` longtext COMMENT '模板内容',
  `is_default` tinyint DEFAULT 0 COMMENT '是否默认(0-否,1-是)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_guide_template_year` (`template_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指南模板库';

-- 验收考核指标项（按课题分类预置）
CREATE TABLE IF NOT EXISTS `mrp_guide_acceptance_index` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `guide_id` varchar(36) NOT NULL COMMENT '指南ID',
  `category_id` varchar(36) DEFAULT NULL COMMENT '课题类目ID（mrp_guide_category）',
  `index_name` varchar(200) DEFAULT NULL COMMENT '指标项名称',
  `index_desc` varchar(500) DEFAULT NULL COMMENT '指标说明',
  `is_required` tinyint DEFAULT 1 COMMENT '是否必查(0-否,1-是)',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_guide_index_guide` (`guide_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验收考核指标项';

-- 指南解读答疑
CREATE TABLE IF NOT EXISTS `mrp_guide_qa` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `guide_id` varchar(36) NOT NULL COMMENT '指南ID',
  `question` varchar(1000) DEFAULT NULL COMMENT '问题',
  `answer` varchar(2000) DEFAULT NULL COMMENT '解答',
  `questioner` varchar(50) DEFAULT NULL COMMENT '提问人',
  `answerer` varchar(50) DEFAULT NULL COMMENT '解答人',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-待解答,1-已解答)',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_guide_qa_guide` (`guide_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指南解读答疑';

-- 指南版本快照（发布时生成，支撑历史比对 / 版本差异对比）
CREATE TABLE IF NOT EXISTS `mrp_guide_version` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `guide_id` varchar(36) NOT NULL COMMENT '指南ID',
  `version_no` varchar(20) DEFAULT NULL COMMENT '版本号（如 V1）',
  `guide_title` varchar(200) DEFAULT NULL COMMENT '指南标题快照',
  `guide_year` varchar(10) DEFAULT NULL COMMENT '指南年度快照',
  `stage` varchar(50) DEFAULT NULL COMMENT '阶段/批次快照',
  `content` longtext COMMENT '指南正文快照',
  `change_note` varchar(500) DEFAULT NULL COMMENT '变更说明',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_guide_version_guide` (`guide_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指南版本快照';

-- ========== 字典 ==========

-- 指南状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000008', '指南状态', 'mrp_guide_status', '课题指南发布状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000000801', '1700000000000000008', '草稿', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000000802', '1700000000000000008', '待发布', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000000803', '1700000000000000008', '已发布', '2', '', 3, 1, 'admin', NOW()),
  ('1700000000000000804', '1700000000000000008', '已归档', '3', '', 4, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 指南阶段/批次
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000009', '指南阶段', 'mrp_guide_stage', '课题指南阶段/批次', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000000901', '1700000000000000009', '年度指南', 'annual', '', 1, 1, 'admin', NOW()),
  ('1700000000000000902', '1700000000000000009', '专项指南', 'special', '', 2, 1, 'admin', NOW()),
  ('1700000000000000903', '1700000000000000009', '定向委托', 'directed', '', 3, 1, 'admin', NOW()),
  ('1700000000000000904', '1700000000000000009', '其他', 'other', '', 4, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;


-- ---------------------------------------------------------------------
-- Source: 2026-08-22_mrp_phase2_application.sql
-- ---------------------------------------------------------------------
-- =====================================================================
-- 气象科研管理平台 · Phase 2 课题申报管理 DDL + 字典
-- 日期: 2026-08-22
-- 说明: 申报主表 / 团队成员 / 版本快照 / 冲突预检记录
--       佐证材料复用公共表 mrp_attachment（biz_type=application），不建独立附件表
-- 幂等: CREATE TABLE IF NOT EXISTS；字典走唯一键冲突更新
-- =====================================================================

-- 课题申报主表
CREATE TABLE IF NOT EXISTS `mrp_application` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `guide_id` varchar(36) DEFAULT NULL COMMENT '指南ID',
  `guide_title` varchar(200) DEFAULT NULL COMMENT '指南标题快照',
  `applicant_id` varchar(36) DEFAULT NULL COMMENT '申请人ID（mrp_researcher.id）',
  `applicant_name` varchar(50) DEFAULT NULL COMMENT '申请人姓名快照',
  `applicant_org` varchar(100) DEFAULT NULL COMMENT '申请人机构快照',
  `applicant_title` varchar(50) DEFAULT NULL COMMENT '申请人职称快照',
  `project_type` varchar(50) DEFAULT NULL COMMENT '项目类型（字典 project_type）',
  `research_field` varchar(100) DEFAULT NULL COMMENT '研究方向（字典 research_field）',
  `project_title` varchar(200) DEFAULT NULL COMMENT '项目名称',
  `project_summary` text COMMENT '研究方案/摘要',
  `budget` decimal(12,2) DEFAULT NULL COMMENT '申请经费(万元)',
  `duration_months` int DEFAULT NULL COMMENT '计划周期(月)',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-草稿,1-已提交,2-形式审查中,3-专家评审中,4-评审通过,5-已拒绝,6-整改中)',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_application_guide` (`guide_id`),
  KEY `idx_mrp_application_applicant` (`applicant_id`),
  KEY `idx_mrp_application_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题申报主表';

-- 团队成员
CREATE TABLE IF NOT EXISTS `mrp_application_member` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `application_id` varchar(36) NOT NULL COMMENT '申报ID',
  `researcher_id` varchar(36) DEFAULT NULL COMMENT '科研人员ID（mrp_researcher.id）',
  `member_name` varchar(50) DEFAULT NULL COMMENT '成员姓名快照',
  `role` varchar(20) DEFAULT NULL COMMENT '角色（leader-负责人,member-参与人）',
  `org_name` varchar(100) DEFAULT NULL COMMENT '机构快照',
  `title` varchar(50) DEFAULT NULL COMMENT '职称快照',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_application_member_app` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申报团队成员';

-- 申报版本快照（提交时生成，支撑断点续填 / 多版本管理）
CREATE TABLE IF NOT EXISTS `mrp_application_version` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `application_id` varchar(36) NOT NULL COMMENT '申报ID',
  `version_no` varchar(20) DEFAULT NULL COMMENT '版本号（如 V1）',
  `project_title` varchar(200) DEFAULT NULL COMMENT '项目名称快照',
  `project_summary` text COMMENT '研究方案快照',
  `content` longtext COMMENT '申报内容完整快照(JSON)',
  `change_note` varchar(500) DEFAULT NULL COMMENT '变更说明',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_application_version_app` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申报版本快照';

-- 冲突预检记录
CREATE TABLE IF NOT EXISTS `mrp_application_conflict_check` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `application_id` varchar(36) NOT NULL COMMENT '申报ID',
  `applicant_id` varchar(36) DEFAULT NULL COMMENT '申请人ID',
  `check_type` varchar(50) DEFAULT NULL COMMENT '预检项（ongoing_project_count/title_qualification/overdue_restricted）',
  `check_result` tinyint DEFAULT 1 COMMENT '结果(1-通过,0-不通过)',
  `check_detail` varchar(500) DEFAULT NULL COMMENT '明细/原因',
  `check_time` datetime DEFAULT NULL COMMENT '预检时间',
  `checker` varchar(50) DEFAULT NULL COMMENT '预检执行方（系统）',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_application_check_app` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申报冲突预检记录';

-- ========== 字典 ==========

-- 申报状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000010', '申报状态', 'mrp_application_status', '课题申报状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000001001', '1700000000000000010', '草稿', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000001002', '1700000000000000010', '已提交', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000001003', '1700000000000000010', '形式审查中', '2', '', 3, 1, 'admin', NOW()),
  ('1700000000000001004', '1700000000000000010', '专家评审中', '3', '', 4, 1, 'admin', NOW()),
  ('1700000000000001005', '1700000000000000010', '评审通过', '4', '', 5, 1, 'admin', NOW()),
  ('1700000000000001006', '1700000000000000010', '已拒绝', '5', '', 6, 1, 'admin', NOW()),
  ('1700000000000001007', '1700000000000000010', '整改中', '6', '', 7, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 团队成员角色
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000011', '成员角色', 'mrp_member_role', '申报团队成员角色', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000001101', '1700000000000000011', '负责人', 'leader', '', 1, 1, 'admin', NOW()),
  ('1700000000000001102', '1700000000000000011', '参与人', 'member', '', 2, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;


-- ---------------------------------------------------------------------
-- Source: 2026-08-24_mrp_phase3_review.sql
-- ---------------------------------------------------------------------
-- =====================================================================
-- 气象科研管理平台 · Phase 3 申报审查 DDL + 字典
-- 日期: 2026-08-24
-- 说明: 形式审查（规则库/结果/整改重报）+ 专家评审（分配/打分/意见）
-- 幂等: CREATE TABLE IF NOT EXISTS；字典走唯一键冲突更新
-- =====================================================================

-- 形式审查规则库（可配置）
CREATE TABLE IF NOT EXISTS `mrp_review_formal_rule` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `rule_name` varchar(100) DEFAULT NULL COMMENT '规则名称',
  `rule_type` varchar(30) DEFAULT NULL COMMENT '规则类型（completeness-完整性,format-格式规范,qualification-资格）',
  `rule_expression` varchar(500) DEFAULT NULL COMMENT '规则说明/校验描述',
  `is_enabled` tinyint DEFAULT 1 COMMENT '是否启用(0-否,1-是)',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='形式审查规则库';

-- 形式审查结果
CREATE TABLE IF NOT EXISTS `mrp_review_formal_result` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `application_id` varchar(36) NOT NULL COMMENT '申报ID',
  `rule_id` varchar(36) DEFAULT NULL COMMENT '规则ID',
  `rule_name` varchar(100) DEFAULT NULL COMMENT '规则名称快照',
  `check_result` tinyint DEFAULT 1 COMMENT '结果(1-通过,0-不通过)',
  `check_detail` varchar(500) DEFAULT NULL COMMENT '问题描述',
  `check_time` datetime DEFAULT NULL COMMENT '预审时间',
  `checker` varchar(50) DEFAULT NULL COMMENT '审查人（系统自动）',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_review_formal_result_app` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='形式审查结果';

-- 形式审查整改-重报闭环记录
CREATE TABLE IF NOT EXISTS `mrp_review_formal_rectification` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `application_id` varchar(36) NOT NULL COMMENT '申报ID',
  `rectification_no` varchar(30) DEFAULT NULL COMMENT '整改单号',
  `issue_desc` varchar(1000) DEFAULT NULL COMMENT '问题清单',
  `rectification_status` tinyint DEFAULT 0 COMMENT '状态(0-待整改,1-已整改重报,2-已复核通过,3-已复核不通过)',
  `deadline` datetime DEFAULT NULL COMMENT '整改截止时间',
  `rectify_content` varchar(2000) DEFAULT NULL COMMENT '整改/重报内容说明',
  `rectify_time` datetime DEFAULT NULL COMMENT '重报时间',
  `review_result` varchar(1000) DEFAULT NULL COMMENT '复核结论',
  `reviewer` varchar(50) DEFAULT NULL COMMENT '复核人',
  `review_time` datetime DEFAULT NULL COMMENT '复核时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_review_rectification_app` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='形式审查整改重报记录';

-- 专家分配（含回避规则记录 / 抽取批次）
CREATE TABLE IF NOT EXISTS `mrp_review_expert_assignment` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `application_id` varchar(36) NOT NULL COMMENT '申报ID',
  `expert_id` varchar(36) DEFAULT NULL COMMENT '专家ID（mrp_expert.id）',
  `expert_name` varchar(50) DEFAULT NULL COMMENT '专家姓名快照',
  `expert_field` varchar(100) DEFAULT NULL COMMENT '专家领域快照',
  `assign_batch` int DEFAULT 1 COMMENT '抽取批次',
  `assign_way` varchar(20) DEFAULT NULL COMMENT '分配方式（manual-人工,auto_random-自动随机）',
  `avoid_checked` tinyint DEFAULT 0 COMMENT '是否已做回避检测(0-否,1-是)',
  `avoid_result` tinyint DEFAULT 1 COMMENT '回避检测结果(1-无回避,0-命中回避)',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-待评审,1-已接受,2-已完成,3-已拒绝)',
  `assigned_time` datetime DEFAULT NULL COMMENT '分配时间',
  `completed_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_review_assignment_app` (`application_id`),
  KEY `idx_mrp_review_assignment_expert` (`expert_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家评审分配';

-- 专家打分（多维度，含评审起止时间）
CREATE TABLE IF NOT EXISTS `mrp_review_score` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `application_id` varchar(36) NOT NULL COMMENT '申报ID',
  `assignment_id` varchar(36) DEFAULT NULL COMMENT '分配ID',
  `expert_id` varchar(36) DEFAULT NULL COMMENT '专家ID',
  `expert_name` varchar(50) DEFAULT NULL COMMENT '专家姓名快照',
  `dimension` varchar(30) DEFAULT NULL COMMENT '打分维度（innovation/feasibility/research_basis）',
  `score` decimal(5,1) DEFAULT NULL COMMENT '分值',
  `review_start_time` datetime DEFAULT NULL COMMENT '评审开始时间',
  `review_end_time` datetime DEFAULT NULL COMMENT '评审结束时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_review_score_app` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家打分';

-- 评审意见文本
CREATE TABLE IF NOT EXISTS `mrp_review_opinion` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `application_id` varchar(36) NOT NULL COMMENT '申报ID',
  `assignment_id` varchar(36) DEFAULT NULL COMMENT '分配ID',
  `expert_id` varchar(36) DEFAULT NULL COMMENT '专家ID',
  `expert_name` varchar(50) DEFAULT NULL COMMENT '专家姓名快照',
  `opinion_type` varchar(20) DEFAULT NULL COMMENT '意见类型（overall-总体意见,issue-问题清单,suggestion-建议）',
  `opinion_content` varchar(2000) DEFAULT NULL COMMENT '意见内容',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_review_opinion_app` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家评审意见';

-- ========== 字典 ==========

-- 形式审查规则类型
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000012', '形式审查规则类型', 'mrp_formal_rule_type', '形式审查规则类型', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000001201', '1700000000000000012', '材料完整性', 'completeness', '', 1, 1, 'admin', NOW()),
  ('1700000000000001202', '1700000000000000012', '格式规范', 'format', '', 2, 1, 'admin', NOW()),
  ('1700000000000001203', '1700000000000000012', '申请人资格', 'qualification', '', 3, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 专家打分维度
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000013', '专家打分维度', 'mrp_review_dimension', '专家评审打分维度', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000001301', '1700000000000000013', '创新性', 'innovation', '', 1, 1, 'admin', NOW()),
  ('1700000000000001302', '1700000000000000013', '可行性', 'feasibility', '', 2, 1, 'admin', NOW()),
  ('1700000000000001303', '1700000000000000013', '研究基础', 'research_basis', '', 3, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 整改状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000014', '整改状态', 'mrp_rectification_status', '形式审查整改状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000001401', '1700000000000000014', '待整改', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000001402', '1700000000000000014', '已整改重报', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000001403', '1700000000000000014', '已复核通过', '2', '', 3, 1, 'admin', NOW()),
  ('1700000000000001404', '1700000000000000014', '已复核不通过', '3', '', 4, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 专家分配状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000015', '专家分配状态', 'mrp_assignment_status', '专家评审分配状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000001501', '1700000000000000015', '待评审', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000001502', '1700000000000000015', '已接受', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000001503', '1700000000000000015', '已完成', '2', '', 3, 1, 'admin', NOW()),
  ('1700000000000001504', '1700000000000000015', '已拒绝', '3', '', 4, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;


-- ---------------------------------------------------------------------
-- Source: 2026-08-24_mrp_phase4_approval.sql
-- ---------------------------------------------------------------------
-- =====================================================================
-- 气象科研管理平台 · Phase 4 立项管理 DDL + 字典
-- 日期: 2026-08-24
-- 说明: 立项项目主表 / 公示记录 / 异议记录 / 任务书
-- 幂等: CREATE TABLE IF NOT EXISTS；字典走唯一键冲突更新
-- =====================================================================

-- 立项项目主表（贯穿执行 / 验收 / 归档全流程）
CREATE TABLE IF NOT EXISTS `mrp_project` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `project_no` varchar(50) DEFAULT NULL COMMENT '项目编号',
  `application_id` varchar(36) NOT NULL COMMENT '申报ID',
  `guide_id` varchar(36) DEFAULT NULL COMMENT '指南ID',
  `project_name` varchar(200) DEFAULT NULL COMMENT '项目名称',
  `project_type` varchar(50) DEFAULT NULL COMMENT '项目类型（字典 project_type）',
  `research_field` varchar(100) DEFAULT NULL COMMENT '研究方向',
  `leader_id` varchar(36) DEFAULT NULL COMMENT '负责人ID（科研人员）',
  `leader_name` varchar(50) DEFAULT NULL COMMENT '负责人姓名快照',
  `leader_org` varchar(100) DEFAULT NULL COMMENT '负责人机构快照',
  `budget` decimal(12,2) DEFAULT NULL COMMENT '立项经费(万元)',
  `duration_months` int DEFAULT NULL COMMENT '计划周期(月)',
  `start_date` date DEFAULT NULL COMMENT '计划开始日期',
  `end_date` date DEFAULT NULL COMMENT '计划结束日期',
  `summary` text COMMENT '项目摘要快照',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-待公示,1-公示中,2-已立项,3-执行中,4-待验收,5-已验收,6-已归档,7-已终止)',
  `approve_time` datetime DEFAULT NULL COMMENT '立项时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mrp_project_application` (`application_id`),
  KEY `idx_mrp_project_leader` (`leader_id`),
  KEY `idx_mrp_project_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='立项项目主表';

-- 立项公示记录
CREATE TABLE IF NOT EXISTS `mrp_approval_publicity` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `publicity_no` varchar(50) DEFAULT NULL COMMENT '公示编号',
  `application_id` varchar(36) NOT NULL COMMENT '申报ID',
  `project_name` varchar(200) DEFAULT NULL COMMENT '公示标题/项目名称快照',
  `start_time` datetime DEFAULT NULL COMMENT '公示开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '公示结束时间',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-公示中,1-已结束,2-已撤销)',
  `result` tinyint DEFAULT 0 COMMENT '公示结论(0-无异议,1-异议处理中,2-有成立异议)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_publicity_app` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='立项公示记录';

-- 立项异议记录（关联 Flowable 核查流程）
CREATE TABLE IF NOT EXISTS `mrp_approval_objection` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `objection_no` varchar(50) DEFAULT NULL COMMENT '异议编号',
  `publicity_id` varchar(36) NOT NULL COMMENT '公示ID',
  `application_id` varchar(36) NOT NULL COMMENT '申报ID',
  `objector_name` varchar(50) DEFAULT NULL COMMENT '异议人',
  `objector_org` varchar(100) DEFAULT NULL COMMENT '异议人机构',
  `objection_content` varchar(2000) DEFAULT NULL COMMENT '异议内容',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-待核查,1-核查中,2-异议成立,3-异议不成立)',
  `process_instance_id` varchar(64) DEFAULT NULL COMMENT 'Flowable 流程实例ID',
  `check_result` varchar(1000) DEFAULT NULL COMMENT '核查结论',
  `checker` varchar(50) DEFAULT NULL COMMENT '核查人',
  `check_time` datetime DEFAULT NULL COMMENT '核查时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_objection_publicity` (`publicity_id`),
  KEY `idx_mrp_objection_app` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='立项异议记录';

-- 任务书
CREATE TABLE IF NOT EXISTS `mrp_task_book` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `task_book_no` varchar(50) DEFAULT NULL COMMENT '任务书编号',
  `project_id` varchar(36) NOT NULL COMMENT '项目ID',
  `application_id` varchar(36) DEFAULT NULL COMMENT '申报ID',
  `title` varchar(200) DEFAULT NULL COMMENT '任务书标题',
  `content` longtext COMMENT '任务书内容（人工起草）',
  `budget` decimal(12,2) DEFAULT NULL COMMENT '任务书经费(万元)',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-草稿,1-待审批,2-已通过,3-已退回)',
  `approver` varchar(50) DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_opinion` varchar(1000) DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_task_book_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目任务书';

-- ========== 字典 ==========

-- 项目状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000016', '项目状态', 'mrp_project_status', '立项项目生命周期状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000001601', '1700000000000000016', '待公示', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000001602', '1700000000000000016', '公示中', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000001603', '1700000000000000016', '已立项', '2', '', 3, 1, 'admin', NOW()),
  ('1700000000000001604', '1700000000000000016', '执行中', '3', '', 4, 1, 'admin', NOW()),
  ('1700000000000001605', '1700000000000000016', '待验收', '4', '', 5, 1, 'admin', NOW()),
  ('1700000000000001606', '1700000000000000016', '已验收', '5', '', 6, 1, 'admin', NOW()),
  ('1700000000000001607', '1700000000000000016', '已归档', '6', '', 7, 1, 'admin', NOW()),
  ('1700000000000001608', '1700000000000000016', '已终止', '7', '', 8, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 公示状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000017', '公示状态', 'mrp_publicity_status', '立项公示状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000001701', '1700000000000000017', '公示中', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000001702', '1700000000000000017', '已结束', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000001703', '1700000000000000017', '已撤销', '2', '', 3, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 异议状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000018', '异议状态', 'mrp_objection_status', '立项异议状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000001801', '1700000000000000018', '待核查', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000001802', '1700000000000000018', '核查中', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000001803', '1700000000000000018', '异议成立', '2', '', 3, 1, 'admin', NOW()),
  ('1700000000000001804', '1700000000000000018', '异议不成立', '3', '', 4, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 任务书状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000019', '任务书状态', 'mrp_task_book_status', '任务书审批状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000001901', '1700000000000000019', '草稿', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000001902', '1700000000000000019', '待审批', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000001903', '1700000000000000019', '已通过', '2', '', 3, 1, 'admin', NOW()),
  ('1700000000000001904', '1700000000000000019', '已退回', '3', '', 4, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;


-- ---------------------------------------------------------------------
-- Source: 2026-08-24_mrp_phase5_execution.sql
-- ---------------------------------------------------------------------
-- =====================================================================
-- 气象科研管理平台 · Phase 5 项目执行管理 DDL + 字典
-- 日期: 2026-08-24
-- 说明: 进展报告 / 中期报告 / 中检评审 / 整改任务 / 项目变更
-- 幂等: CREATE TABLE IF NOT EXISTS；字典走唯一键冲突更新
-- =====================================================================

-- 进展报告
CREATE TABLE IF NOT EXISTS `mrp_progress_report` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `project_id` varchar(36) NOT NULL COMMENT '项目ID',
  `report_no` varchar(50) DEFAULT NULL COMMENT '报告编号',
  `report_title` varchar(200) DEFAULT NULL COMMENT '报告标题',
  `report_period` varchar(50) DEFAULT NULL COMMENT '报告期（如 2026Q1 / 第3期）',
  `content` text COMMENT '进展内容',
  `progress_percent` int DEFAULT 0 COMMENT '完成度(%)0-100',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-草稿,1-已提交)',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `submit_by` varchar(50) DEFAULT NULL COMMENT '提交人',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_progress_report_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目进展报告';

-- 中期报告
CREATE TABLE IF NOT EXISTS `mrp_midcheck_report` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `project_id` varchar(36) NOT NULL COMMENT '项目ID',
  `report_no` varchar(50) DEFAULT NULL COMMENT '报告编号',
  `report_title` varchar(200) DEFAULT NULL COMMENT '报告标题',
  `content` text COMMENT '中期报告正文',
  `similar_rate` decimal(5,2) DEFAULT NULL COMMENT '查重率(%)(vs 任务书)',
  `similar_detail` varchar(500) DEFAULT NULL COMMENT '查重说明',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-草稿,1-已提交,2-评审中,3-已通过,4-已退回)',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `submit_by` varchar(50) DEFAULT NULL COMMENT '提交人',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_midcheck_report_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中期检查报告';

-- 中检评审
CREATE TABLE IF NOT EXISTS `mrp_midcheck_review` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `midcheck_report_id` varchar(36) NOT NULL COMMENT '中期报告ID',
  `project_id` varchar(36) NOT NULL COMMENT '项目ID',
  `reviewer` varchar(50) DEFAULT NULL COMMENT '评审人',
  `review_result` tinyint DEFAULT 1 COMMENT '结果(1-通过,0-退回)',
  `review_opinion` varchar(1000) DEFAULT NULL COMMENT '评审意见',
  `review_time` datetime DEFAULT NULL COMMENT '评审时间',
  `process_instance_id` varchar(64) DEFAULT NULL COMMENT 'Flowable 流程实例ID',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_midcheck_review_report` (`midcheck_report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中期检查评审';

-- 整改任务（看板）
CREATE TABLE IF NOT EXISTS `mrp_rectification_task` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `project_id` varchar(36) NOT NULL COMMENT '项目ID',
  `midcheck_review_id` varchar(36) DEFAULT NULL COMMENT '来源评审ID（中检退回生成）',
  `task_title` varchar(200) DEFAULT NULL COMMENT '整改任务标题',
  `task_desc` varchar(1000) DEFAULT NULL COMMENT '任务说明',
  `assignee` varchar(50) DEFAULT NULL COMMENT '责任人',
  `deadline` datetime DEFAULT NULL COMMENT '整改期限',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-待处理,1-处理中,2-已完成,3-已关闭)',
  `handle_content` varchar(2000) DEFAULT NULL COMMENT '处理说明',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_rectification_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='整改任务';

-- 项目变更申请
CREATE TABLE IF NOT EXISTS `mrp_change_request` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `project_id` varchar(36) NOT NULL COMMENT '项目ID',
  `change_no` varchar(50) DEFAULT NULL COMMENT '变更编号',
  `change_type` varchar(30) DEFAULT NULL COMMENT '变更类型（字典 change_type）',
  `change_title` varchar(200) DEFAULT NULL COMMENT '变更标题',
  `change_content` varchar(2000) DEFAULT NULL COMMENT '变更内容',
  `budget_change` decimal(12,2) DEFAULT NULL COMMENT '预算变动金额(万元,0-无)',
  `impact_level` varchar(10) DEFAULT 'low' COMMENT '影响等级(high-高风险,low-低风险)（规则评估）',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-草稿,1-待审批,2-已通过,3-已拒绝)',
  `apply_by` varchar(50) DEFAULT NULL COMMENT '申请人',
  `apply_time` datetime DEFAULT NULL COMMENT '申请时间',
  `process_instance_id` varchar(64) DEFAULT NULL COMMENT 'Flowable 流程实例ID',
  `approver` varchar(50) DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_opinion` varchar(1000) DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_change_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目变更申请';

-- ========== 字典 ==========

-- 中期报告状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000020', '中期报告状态', 'mrp_midcheck_status', '中期检查报告状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000002001', '1700000000000000020', '草稿', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000002002', '1700000000000000020', '已提交', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000002003', '1700000000000000020', '评审中', '2', '', 3, 1, 'admin', NOW()),
  ('1700000000000002004', '1700000000000000020', '已通过', '3', '', 4, 1, 'admin', NOW()),
  ('1700000000000002005', '1700000000000000020', '已退回', '4', '', 5, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 整改任务状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000021', '整改任务状态', 'mrp_rectification_task_status', '整改任务看板状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000002101', '1700000000000000021', '待处理', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000002102', '1700000000000000021', '处理中', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000002103', '1700000000000000021', '已完成', '2', '', 3, 1, 'admin', NOW()),
  ('1700000000000002104', '1700000000000000021', '已关闭', '3', '', 4, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 项目变更状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000022', '变更状态', 'mrp_change_status', '项目变更申请状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000002201', '1700000000000000022', '草稿', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000002202', '1700000000000000022', '待审批', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000002203', '1700000000000000022', '已通过', '2', '', 3, 1, 'admin', NOW()),
  ('1700000000000002204', '1700000000000000022', '已拒绝', '3', '', 4, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 变更影响等级
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000023', '变更影响等级', 'mrp_change_impact', '项目变更影响等级（规则评估）', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000002301', '1700000000000000023', '高风险', 'high', '', 1, 1, 'admin', NOW()),
  ('1700000000000002302', '1700000000000000023', '低风险', 'low', '', 2, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;


-- ---------------------------------------------------------------------
-- Source: 2026-08-25_mrp_phase6_acceptance.sql
-- ---------------------------------------------------------------------
-- =====================================================================
-- 气象科研管理平台 · Phase 6 验收管理 DDL + 字典
-- 日期: 2026-08-25
-- 说明: 验收申请 / 验收会议 / 验收打分 / 验收问题库 / 延期处理
-- 幂等: CREATE TABLE IF NOT EXISTS；字典走唯一键冲突更新
-- =====================================================================

-- 验收申请
CREATE TABLE IF NOT EXISTS `mrp_acceptance_application` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `project_id` varchar(36) NOT NULL COMMENT '项目ID',
  `application_no` varchar(50) DEFAULT NULL COMMENT '验收申请编号',
  `material_list` text COMMENT '验收材料清单（材料梳理）',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-草稿,1-已提交,2-初审中,3-待专家评审,4-评审中,5-已通过,6-不合格,7-已延期)',
  `pre_accept_ai` varchar(500) DEFAULT NULL COMMENT 'AI 预验收说明（占位）',
  `pass_rate_prediction` varchar(500) DEFAULT NULL COMMENT '通过率预测（占位）',
  `process_instance_id` varchar(64) DEFAULT NULL COMMENT 'Flowable 验收审批流程实例ID',
  `apply_by` varchar(50) DEFAULT NULL COMMENT '申请人',
  `apply_time` datetime DEFAULT NULL COMMENT '申请时间',
  `pre_reviewer` varchar(50) DEFAULT NULL COMMENT '初审人',
  `pre_review_opinion` varchar(1000) DEFAULT NULL COMMENT '初审意见',
  `pre_review_time` datetime DEFAULT NULL COMMENT '初审时间',
  `finish_time` datetime DEFAULT NULL COMMENT '验收完成时间',
  `conclusion` varchar(1000) DEFAULT NULL COMMENT '验收结论',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_acceptance_app_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验收申请';

-- 验收会议（会议室 / 设备预约）
CREATE TABLE IF NOT EXISTS `mrp_acceptance_meeting` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `acceptance_application_id` varchar(36) NOT NULL COMMENT '验收申请ID',
  `project_id` varchar(36) NOT NULL COMMENT '项目ID',
  `meeting_name` varchar(200) DEFAULT NULL COMMENT '会议名称',
  `meeting_time` datetime DEFAULT NULL COMMENT '会议时间',
  `location` varchar(100) DEFAULT NULL COMMENT '会议室',
  `equipment` varchar(200) DEFAULT NULL COMMENT '设备（投影/录播等）',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-待组织,1-已预约,2-已召开,3-已取消)',
  `organizer` varchar(50) DEFAULT NULL COMMENT '组织人',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_acceptance_meeting_app` (`acceptance_application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验收会议';

-- 验收打分
CREATE TABLE IF NOT EXISTS `mrp_acceptance_score` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `acceptance_application_id` varchar(36) NOT NULL COMMENT '验收申请ID',
  `project_id` varchar(36) NOT NULL COMMENT '项目ID',
  `expert_id` varchar(36) DEFAULT NULL COMMENT '专家ID（mrp_expert.id）',
  `expert_name` varchar(50) DEFAULT NULL COMMENT '专家姓名快照',
  `dimension` varchar(30) DEFAULT NULL COMMENT '打分维度（completion/achievement/presentation）',
  `score` decimal(5,1) DEFAULT NULL COMMENT '分值',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_acceptance_score_app` (`acceptance_application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验收打分';

-- 验收问题库（专家辅助）
CREATE TABLE IF NOT EXISTS `mrp_acceptance_question_bank` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `question` varchar(1000) DEFAULT NULL COMMENT '问题',
  `answer` varchar(2000) DEFAULT NULL COMMENT '参考答案',
  `category` varchar(50) DEFAULT NULL COMMENT '分类/领域',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验收问题库';

-- 延期处理记录（含惩罚机制，供 Phase 2 冲突预检读取受限名单）
CREATE TABLE IF NOT EXISTS `mrp_acceptance_overdue` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `project_id` varchar(36) NOT NULL COMMENT '项目ID',
  `project_name` varchar(200) DEFAULT NULL COMMENT '项目名称快照',
  `deadline` datetime DEFAULT NULL COMMENT '应验收时间',
  `overdue_days` int DEFAULT 0 COMMENT '逾期天数',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-处理中,1-已通报,2-已限制申报,3-已整改销号)',
  `handle_result` varchar(1000) DEFAULT NULL COMMENT '处理结果',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_acceptance_overdue_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验收延期处理记录';

-- ========== 字典 ==========

-- 验收状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000024', '验收状态', 'mrp_acceptance_status', '验收申请状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000002401', '1700000000000000024', '草稿', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000002402', '1700000000000000024', '已提交', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000002403', '1700000000000000024', '初审中', '2', '', 3, 1, 'admin', NOW()),
  ('1700000000000002404', '1700000000000000024', '待专家评审', '3', '', 4, 1, 'admin', NOW()),
  ('1700000000000002405', '1700000000000000024', '评审中', '4', '', 5, 1, 'admin', NOW()),
  ('1700000000000002406', '1700000000000000024', '已通过', '5', '', 6, 1, 'admin', NOW()),
  ('1700000000000002407', '1700000000000000024', '不合格', '6', '', 7, 1, 'admin', NOW()),
  ('1700000000000002408', '1700000000000000024', '已延期', '7', '', 8, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 验收会议状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000025', '验收会议状态', 'mrp_acceptance_meeting_status', '验收会议状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000002501', '1700000000000000025', '待组织', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000002502', '1700000000000000025', '已预约', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000002503', '1700000000000000025', '已召开', '2', '', 3, 1, 'admin', NOW()),
  ('1700000000000002504', '1700000000000000025', '已取消', '3', '', 4, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 延期处理状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000026', '延期处理状态', 'mrp_acceptance_overdue_status', '验收延期处理状态（惩罚机制）', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000002601', '1700000000000000026', '处理中', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000002602', '1700000000000000026', '已通报', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000002603', '1700000000000000026', '已限制申报', '2', '', 3, 1, 'admin', NOW()),
  ('1700000000000002604', '1700000000000000026', '已整改销号', '3', '', 4, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 验收打分维度
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000027', '验收打分维度', 'mrp_acceptance_dimension', '验收专家打分维度', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000002701', '1700000000000000027', '任务完成度', 'completion', '', 1, 1, 'admin', NOW()),
  ('1700000000000002702', '1700000000000000027', '成果质量', 'achievement', '', 2, 1, 'admin', NOW()),
  ('1700000000000002703', '1700000000000000027', '汇报答辩', 'presentation', '', 3, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;


-- ---------------------------------------------------------------------
-- Source: 2026-08-26_mrp_phase7_archive.sql
-- ---------------------------------------------------------------------
-- =====================================================================
-- 气象科研管理平台 · Phase 7 项目归档管理 DDL + 字典
-- 日期: 2026-08-26
-- 说明: 归档主表 / 归档材料明细 / 归档模板 / 归档操作日志
-- 幂等: CREATE TABLE IF NOT EXISTS；字典走唯一键冲突更新
-- =====================================================================

-- 归档主表
CREATE TABLE IF NOT EXISTS `mrp_archive` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `project_id` varchar(36) NOT NULL COMMENT '项目ID',
  `archive_no` varchar(50) DEFAULT NULL COMMENT '归档编号',
  `archive_title` varchar(200) DEFAULT NULL COMMENT '归档标题',
  `project_no` varchar(50) DEFAULT NULL COMMENT '项目编号快照',
  `project_name` varchar(200) DEFAULT NULL COMMENT '项目名称快照',
  `category` varchar(50) DEFAULT NULL COMMENT '归档类别（项目类型快照）',
  `status` tinyint DEFAULT 0 COMMENT '状态(0-准备中,1-已归档,2-已撤销)',
  `package_json` longtext COMMENT '归档电子档案包(JSON)',
  `archive_time` datetime DEFAULT NULL COMMENT '归档时间',
  `archive_by` varchar(50) DEFAULT NULL COMMENT '归档人',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_archive_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目归档主表';

-- 归档材料明细（全流程材料标准化归档）
CREATE TABLE IF NOT EXISTS `mrp_archive_item` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `archive_id` varchar(36) NOT NULL COMMENT '归档ID',
  `project_id` varchar(36) NOT NULL COMMENT '项目ID',
  `item_type` varchar(30) DEFAULT NULL COMMENT '材料类型（application/task_book/midcheck/progress/acceptance/achievement/other）',
  `item_name` varchar(200) DEFAULT NULL COMMENT '材料名称',
  `source_id` varchar(36) DEFAULT NULL COMMENT '来源业务ID（如申报/任务书/报告ID）',
  `file_id` varchar(36) DEFAULT NULL COMMENT '附件ID（mrp_attachment.id，可空）',
  `file_url` varchar(500) DEFAULT NULL COMMENT '附件地址（可空）',
  `file_size` bigint DEFAULT NULL COMMENT '附件大小(字节)',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_archive_item_archive` (`archive_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归档材料明细';

-- 归档模板（按项目类别）
CREATE TABLE IF NOT EXISTS `mrp_archive_template` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `template_name` varchar(100) DEFAULT NULL COMMENT '模板名称',
  `category` varchar(50) DEFAULT NULL COMMENT '适用项目类别',
  `template_content` longtext COMMENT '模板内容（材料清单等）',
  `is_default` tinyint DEFAULT 0 COMMENT '是否默认(0-否,1-是)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归档模板';

-- 归档操作日志（查阅 / 下载 / 打印）
CREATE TABLE IF NOT EXISTS `mrp_archive_log` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `archive_id` varchar(36) NOT NULL COMMENT '归档ID',
  `project_id` varchar(36) NOT NULL COMMENT '项目ID',
  `action_type` varchar(20) DEFAULT NULL COMMENT '操作类型（view-查阅,download-下载,print-打印）',
  `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_mrp_archive_log_archive` (`archive_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归档操作日志';

-- ========== 字典 ==========

-- 归档状态
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000028', '归档状态', 'mrp_archive_status', '项目归档状态', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000002801', '1700000000000000028', '准备中', '0', '', 1, 1, 'admin', NOW()),
  ('1700000000000002802', '1700000000000000028', '已归档', '1', '', 2, 1, 'admin', NOW()),
  ('1700000000000002803', '1700000000000000028', '已撤销', '2', '', 3, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 归档材料类型
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000029', '归档材料类型', 'mrp_archive_item_type', '归档材料类型', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000002901', '1700000000000000029', '申报书', 'application', '', 1, 1, 'admin', NOW()),
  ('1700000000000002902', '1700000000000000029', '任务书', 'task_book', '', 2, 1, 'admin', NOW()),
  ('1700000000000002903', '1700000000000000029', '中期报告', 'midcheck', '', 3, 1, 'admin', NOW()),
  ('1700000000000002904', '1700000000000000029', '进展报告', 'progress', '', 4, 1, 'admin', NOW()),
  ('1700000000000002905', '1700000000000000029', '验收材料', 'acceptance', '', 5, 1, 'admin', NOW()),
  ('1700000000000002906', '1700000000000000029', '成果材料', 'achievement', '', 6, 1, 'admin', NOW()),
  ('1700000000000002907', '1700000000000000029', '其他', 'other', '', 7, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;

-- 归档操作类型
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, type)
VALUES ('1700000000000000030', '归档操作类型', 'mrp_archive_log_action', '归档查阅/下载/打印日志类型', 0, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name), description = VALUES(description), del_flag = 0;

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time)
VALUES
  ('1700000000000003001', '1700000000000000030', '查阅', 'view', '', 1, 1, 'admin', NOW()),
  ('1700000000000003002', '1700000000000000030', '下载', 'download', '', 2, 1, 'admin', NOW()),
  ('1700000000000003003', '1700000000000000030', '打印', 'print', '', 3, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text), item_value = VALUES(item_value), sort_order = VALUES(sort_order), status = 1;


