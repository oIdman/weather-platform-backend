-- JeecgBoot 3.9.5 workflow installation package (MySQL)
--
-- Prerequisites:
--   1. Select the target JeecgBoot database (normally jeecg_boot) before running this file.
--   2. Import jeecg-boot/db/jeecgboot-mysql-5.7.sql first; this package only contains
--      Flowable 8.0.0 engine tables and Jeecg workflow increments.
--
-- This file is generated from the Flowable 8.0.0 MySQL schema bundled in
-- flowable-engine-common/flowable-engine 8.0.0 and the workflow Flyway migrations
-- in jeecg-system-start. It intentionally does not contain USE, DROP, or demo data.
-- Run it once on a backup of the target database, then start jeecg-system-start.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ===== Flowable common schema =====
create table ACT_GE_PROPERTY (
    NAME_ varchar(64),
    VALUE_ varchar(300),
    REV_ integer,
    primary key (NAME_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_GE_BYTEARRAY (
    ID_ varchar(64),
    REV_ integer,
    NAME_ varchar(255),
    DEPLOYMENT_ID_ varchar(64),
    BYTES_ LONGBLOB,
    GENERATED_ TINYINT,
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

insert into ACT_GE_PROPERTY
values ('common.schema.version', '8.0.0.0', 1);

insert into ACT_GE_PROPERTY
values ('next.dbid', '1', 1);


create table ACT_RU_ENTITYLINK (
    ID_ varchar(64),
    REV_ integer,
    CREATE_TIME_ datetime(3),
    LINK_TYPE_ varchar(255),
    SCOPE_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    SCOPE_DEFINITION_ID_ varchar(255),
    PARENT_ELEMENT_ID_ varchar(255),
    REF_SCOPE_ID_ varchar(255),
    REF_SCOPE_TYPE_ varchar(255),
    REF_SCOPE_DEFINITION_ID_ varchar(255),
    ROOT_SCOPE_ID_ varchar(255),
    ROOT_SCOPE_TYPE_ varchar(255),
    HIERARCHY_TYPE_ varchar(255),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_ENT_LNK_SCOPE on ACT_RU_ENTITYLINK(SCOPE_ID_, SCOPE_TYPE_, LINK_TYPE_);
create index ACT_IDX_ENT_LNK_REF_SCOPE on ACT_RU_ENTITYLINK(REF_SCOPE_ID_, REF_SCOPE_TYPE_, LINK_TYPE_);
create index ACT_IDX_ENT_LNK_ROOT_SCOPE on ACT_RU_ENTITYLINK(ROOT_SCOPE_ID_, ROOT_SCOPE_TYPE_, LINK_TYPE_);
create index ACT_IDX_ENT_LNK_SCOPE_DEF on ACT_RU_ENTITYLINK(SCOPE_DEFINITION_ID_, SCOPE_TYPE_, LINK_TYPE_);

create table ACT_HI_ENTITYLINK (
    ID_ varchar(64),
    LINK_TYPE_ varchar(255),
    CREATE_TIME_ datetime(3),
    SCOPE_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    SCOPE_DEFINITION_ID_ varchar(255),
    PARENT_ELEMENT_ID_ varchar(255),
    REF_SCOPE_ID_ varchar(255),
    REF_SCOPE_TYPE_ varchar(255),
    REF_SCOPE_DEFINITION_ID_ varchar(255),
    ROOT_SCOPE_ID_ varchar(255),
    ROOT_SCOPE_TYPE_ varchar(255),
    HIERARCHY_TYPE_ varchar(255),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_HI_ENT_LNK_SCOPE on ACT_HI_ENTITYLINK(SCOPE_ID_, SCOPE_TYPE_, LINK_TYPE_);
create index ACT_IDX_HI_ENT_LNK_REF_SCOPE on ACT_HI_ENTITYLINK(REF_SCOPE_ID_, REF_SCOPE_TYPE_, LINK_TYPE_);
create index ACT_IDX_HI_ENT_LNK_ROOT_SCOPE on ACT_HI_ENTITYLINK(ROOT_SCOPE_ID_, ROOT_SCOPE_TYPE_, LINK_TYPE_);
create index ACT_IDX_HI_ENT_LNK_SCOPE_DEF on ACT_HI_ENTITYLINK(SCOPE_DEFINITION_ID_, SCOPE_TYPE_, LINK_TYPE_);


create table ACT_RU_IDENTITYLINK (
    ID_ varchar(64),
    REV_ integer,
    GROUP_ID_ varchar(255),
    TYPE_ varchar(255),
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    SCOPE_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    SCOPE_DEFINITION_ID_ varchar(255),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_IDENT_LNK_USER on ACT_RU_IDENTITYLINK(USER_ID_);
create index ACT_IDX_IDENT_LNK_GROUP on ACT_RU_IDENTITYLINK(GROUP_ID_);
create index ACT_IDX_IDENT_LNK_SCOPE on ACT_RU_IDENTITYLINK(SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_IDENT_LNK_SUB_SCOPE on ACT_RU_IDENTITYLINK(SUB_SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_IDENT_LNK_SCOPE_DEF on ACT_RU_IDENTITYLINK(SCOPE_DEFINITION_ID_, SCOPE_TYPE_);

create table ACT_HI_IDENTITYLINK (
    ID_ varchar(64),
    GROUP_ID_ varchar(255),
    TYPE_ varchar(255),
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    CREATE_TIME_ datetime(3),
    PROC_INST_ID_ varchar(64),
    SCOPE_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    SCOPE_DEFINITION_ID_ varchar(255),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_HI_IDENT_LNK_USER on ACT_HI_IDENTITYLINK(USER_ID_);
create index ACT_IDX_HI_IDENT_LNK_SCOPE on ACT_HI_IDENTITYLINK(SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_HI_IDENT_LNK_SUB_SCOPE on ACT_HI_IDENTITYLINK(SUB_SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_HI_IDENT_LNK_SCOPE_DEF on ACT_HI_IDENTITYLINK(SCOPE_DEFINITION_ID_, SCOPE_TYPE_);


create table ACT_RU_JOB (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    TYPE_ varchar(255) NOT NULL,
    LOCK_EXP_TIME_ timestamp(3) NULL,
    LOCK_OWNER_ varchar(255),
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    ELEMENT_ID_ varchar(255),
    ELEMENT_NAME_ varchar(255),
    SCOPE_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    SCOPE_DEFINITION_ID_ varchar(255),
    CORRELATION_ID_ varchar(255),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp(3) NULL,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    CUSTOM_VALUES_ID_ varchar(64),
    CREATE_TIME_ timestamp(3) NULL,
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_TIMER_JOB (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    TYPE_ varchar(255) NOT NULL,
    LOCK_EXP_TIME_ timestamp(3) NULL,
    LOCK_OWNER_ varchar(255),
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    ELEMENT_ID_ varchar(255),
    ELEMENT_NAME_ varchar(255),
    SCOPE_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    SCOPE_DEFINITION_ID_ varchar(255),
    CORRELATION_ID_ varchar(255),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp(3) NULL,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    CUSTOM_VALUES_ID_ varchar(64),
    CREATE_TIME_ timestamp(3) NULL,
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_SUSPENDED_JOB (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    TYPE_ varchar(255) NOT NULL,
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    ELEMENT_ID_ varchar(255),
    ELEMENT_NAME_ varchar(255),
    SCOPE_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    SCOPE_DEFINITION_ID_ varchar(255),
    CORRELATION_ID_ varchar(255),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp(3) NULL,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    CUSTOM_VALUES_ID_ varchar(64),
    CREATE_TIME_ timestamp(3) NULL,
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_DEADLETTER_JOB (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    TYPE_ varchar(255) NOT NULL,
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    ELEMENT_ID_ varchar(255),
    ELEMENT_NAME_ varchar(255),
    SCOPE_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    SCOPE_DEFINITION_ID_ varchar(255),
    CORRELATION_ID_ varchar(255),
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp(3) NULL,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    CUSTOM_VALUES_ID_ varchar(64),
    CREATE_TIME_ timestamp(3) NULL,
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_HISTORY_JOB (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    LOCK_EXP_TIME_ timestamp(3) NULL,
    LOCK_OWNER_ varchar(255),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    CUSTOM_VALUES_ID_ varchar(64),
    ADV_HANDLER_CFG_ID_ varchar(64),
    CREATE_TIME_ timestamp(3) NULL,
    SCOPE_TYPE_ varchar(255),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_EXTERNAL_JOB (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    TYPE_ varchar(255) NOT NULL,
    LOCK_EXP_TIME_ timestamp(3) NULL,
    LOCK_OWNER_ varchar(255),
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    ELEMENT_ID_ varchar(255),
    ELEMENT_NAME_ varchar(255),
    SCOPE_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    SCOPE_DEFINITION_ID_ varchar(255),
    CORRELATION_ID_ varchar(255),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp(3) NULL,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    CUSTOM_VALUES_ID_ varchar(64),
    CREATE_TIME_ timestamp(3) NULL,
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_JOB_EXCEPTION_STACK_ID on ACT_RU_JOB(EXCEPTION_STACK_ID_);
create index ACT_IDX_JOB_CUSTOM_VALUES_ID on ACT_RU_JOB(CUSTOM_VALUES_ID_);
create index ACT_IDX_JOB_CORRELATION_ID on ACT_RU_JOB(CORRELATION_ID_);

create index ACT_IDX_TIMER_JOB_EXCEPTION_STACK_ID on ACT_RU_TIMER_JOB(EXCEPTION_STACK_ID_);
create index ACT_IDX_TIMER_JOB_CUSTOM_VALUES_ID on ACT_RU_TIMER_JOB(CUSTOM_VALUES_ID_);
create index ACT_IDX_TIMER_JOB_CORRELATION_ID on ACT_RU_TIMER_JOB(CORRELATION_ID_);
create index ACT_IDX_TIMER_JOB_DUEDATE on ACT_RU_TIMER_JOB(DUEDATE_);

create index ACT_IDX_SUSPENDED_JOB_EXCEPTION_STACK_ID on ACT_RU_SUSPENDED_JOB(EXCEPTION_STACK_ID_);
create index ACT_IDX_SUSPENDED_JOB_CUSTOM_VALUES_ID on ACT_RU_SUSPENDED_JOB(CUSTOM_VALUES_ID_);
create index ACT_IDX_SUSPENDED_JOB_CORRELATION_ID on ACT_RU_SUSPENDED_JOB(CORRELATION_ID_);

create index ACT_IDX_DEADLETTER_JOB_EXCEPTION_STACK_ID on ACT_RU_DEADLETTER_JOB(EXCEPTION_STACK_ID_);
create index ACT_IDX_DEADLETTER_JOB_CUSTOM_VALUES_ID on ACT_RU_DEADLETTER_JOB(CUSTOM_VALUES_ID_);
create index ACT_IDX_DEADLETTER_JOB_CORRELATION_ID on ACT_RU_DEADLETTER_JOB(CORRELATION_ID_);

create index ACT_IDX_EXTERNAL_JOB_EXCEPTION_STACK_ID on ACT_RU_EXTERNAL_JOB(EXCEPTION_STACK_ID_);
create index ACT_IDX_EXTERNAL_JOB_CUSTOM_VALUES_ID on ACT_RU_EXTERNAL_JOB(CUSTOM_VALUES_ID_);
create index ACT_IDX_EXTERNAL_JOB_CORRELATION_ID on ACT_RU_EXTERNAL_JOB(CORRELATION_ID_);

alter table ACT_RU_JOB
    add constraint ACT_FK_JOB_EXCEPTION
    foreign key (EXCEPTION_STACK_ID_)
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_JOB
    add constraint ACT_FK_JOB_CUSTOM_VALUES
    foreign key (CUSTOM_VALUES_ID_)
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_TIMER_JOB
    add constraint ACT_FK_TIMER_JOB_EXCEPTION
    foreign key (EXCEPTION_STACK_ID_)
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_TIMER_JOB
    add constraint ACT_FK_TIMER_JOB_CUSTOM_VALUES
    foreign key (CUSTOM_VALUES_ID_)
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_SUSPENDED_JOB
    add constraint ACT_FK_SUSPENDED_JOB_EXCEPTION
    foreign key (EXCEPTION_STACK_ID_)
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_SUSPENDED_JOB
    add constraint ACT_FK_SUSPENDED_JOB_CUSTOM_VALUES
    foreign key (CUSTOM_VALUES_ID_)
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_DEADLETTER_JOB
    add constraint ACT_FK_DEADLETTER_JOB_EXCEPTION
    foreign key (EXCEPTION_STACK_ID_)
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_DEADLETTER_JOB
    add constraint ACT_FK_DEADLETTER_JOB_CUSTOM_VALUES
    foreign key (CUSTOM_VALUES_ID_)
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_EXTERNAL_JOB
    add constraint ACT_FK_EXTERNAL_JOB_EXCEPTION
    foreign key (EXCEPTION_STACK_ID_)
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_EXTERNAL_JOB
    add constraint ACT_FK_EXTERNAL_JOB_CUSTOM_VALUES
    foreign key (CUSTOM_VALUES_ID_)
    references ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_JOB_SCOPE on ACT_RU_JOB(SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_JOB_SUB_SCOPE on ACT_RU_JOB(SUB_SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_JOB_SCOPE_DEF on ACT_RU_JOB(SCOPE_DEFINITION_ID_, SCOPE_TYPE_);

create index ACT_IDX_TJOB_SCOPE on ACT_RU_TIMER_JOB(SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_TJOB_SUB_SCOPE on ACT_RU_TIMER_JOB(SUB_SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_TJOB_SCOPE_DEF on ACT_RU_TIMER_JOB(SCOPE_DEFINITION_ID_, SCOPE_TYPE_);

create index ACT_IDX_SJOB_SCOPE on ACT_RU_SUSPENDED_JOB(SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_SJOB_SUB_SCOPE on ACT_RU_SUSPENDED_JOB(SUB_SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_SJOB_SCOPE_DEF on ACT_RU_SUSPENDED_JOB(SCOPE_DEFINITION_ID_, SCOPE_TYPE_);

create index ACT_IDX_DJOB_SCOPE on ACT_RU_DEADLETTER_JOB(SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_DJOB_SUB_SCOPE on ACT_RU_DEADLETTER_JOB(SUB_SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_DJOB_SCOPE_DEF on ACT_RU_DEADLETTER_JOB(SCOPE_DEFINITION_ID_, SCOPE_TYPE_);

create index ACT_IDX_EJOB_SCOPE on ACT_RU_EXTERNAL_JOB(SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_EJOB_SUB_SCOPE on ACT_RU_EXTERNAL_JOB(SUB_SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_EJOB_SCOPE_DEF on ACT_RU_EXTERNAL_JOB(SCOPE_DEFINITION_ID_, SCOPE_TYPE_);

create table FLW_RU_BATCH (
    ID_ varchar(64) not null,
    REV_ integer,
    TYPE_ varchar(64) not null,
    SEARCH_KEY_ varchar(255),
    SEARCH_KEY2_ varchar(255),
    CREATE_TIME_ datetime(3) not null,
    COMPLETE_TIME_ datetime(3),
    STATUS_ varchar(255),
    BATCH_DOC_ID_ varchar(64),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table FLW_RU_BATCH_PART (
    ID_ varchar(64) not null,
    REV_ integer,
    BATCH_ID_ varchar(64),
    TYPE_ varchar(64) not null,
    SCOPE_ID_ varchar(64),
    SUB_SCOPE_ID_ varchar(64),
    SCOPE_TYPE_ varchar(64),
    SEARCH_KEY_ varchar(255),
    SEARCH_KEY2_ varchar(255),
    CREATE_TIME_ datetime(3) not null,
    COMPLETE_TIME_ datetime(3),
    STATUS_ varchar(255),
    RESULT_DOC_ID_ varchar(64),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index FLW_IDX_BATCH_PART on FLW_RU_BATCH_PART(BATCH_ID_);

alter table FLW_RU_BATCH_PART
    add constraint FLW_FK_BATCH_PART_PARENT
    foreign key (BATCH_ID_)
    references FLW_RU_BATCH (ID_);

create table ACT_RU_TASK (
    ID_ varchar(64),
    REV_ integer,
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    TASK_DEF_ID_ varchar(64),
    SCOPE_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    SCOPE_DEFINITION_ID_ varchar(255),
    PROPAGATED_STAGE_INST_ID_ varchar(255),
    STATE_ varchar(255),
    NAME_ varchar(255),
    PARENT_TASK_ID_ varchar(64),
    DESCRIPTION_ varchar(4000),
    TASK_DEF_KEY_ varchar(255),
    OWNER_ varchar(255),
    ASSIGNEE_ varchar(255),
    DELEGATION_ varchar(64),
    PRIORITY_ integer,
    CREATE_TIME_ timestamp(3) NULL,
    IN_PROGRESS_TIME_ datetime(3),
    IN_PROGRESS_STARTED_BY_ varchar(255),
    CLAIM_TIME_ datetime(3),
    CLAIMED_BY_ varchar(255),
    SUSPENDED_TIME_ datetime(3),
    SUSPENDED_BY_ varchar(255),
    IN_PROGRESS_DUE_DATE_ datetime(3),
    DUE_DATE_ datetime(3),
    CATEGORY_ varchar(255),
    SUSPENSION_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    FORM_KEY_ varchar(255),
    IS_COUNT_ENABLED_ TINYINT,
    VAR_COUNT_ integer,
    ID_LINK_COUNT_ integer,
    SUB_TASK_COUNT_ integer,
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_TASK_CREATE on ACT_RU_TASK(CREATE_TIME_);
create index ACT_IDX_TASK_SCOPE on ACT_RU_TASK(SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_TASK_SUB_SCOPE on ACT_RU_TASK(SUB_SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_TASK_SCOPE_DEF on ACT_RU_TASK(SCOPE_DEFINITION_ID_, SCOPE_TYPE_);

create table ACT_HI_TASKINST (
    ID_ varchar(64) not null,
    REV_ integer default 1,
    PROC_DEF_ID_ varchar(64),
    TASK_DEF_ID_ varchar(64),
    TASK_DEF_KEY_ varchar(255),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    SCOPE_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    SCOPE_DEFINITION_ID_ varchar(255),
    PROPAGATED_STAGE_INST_ID_ varchar(255),
    STATE_ varchar(255),
    NAME_ varchar(255),
    PARENT_TASK_ID_ varchar(64),
    DESCRIPTION_ varchar(4000),
    OWNER_ varchar(255),
    ASSIGNEE_ varchar(255),
    START_TIME_ datetime(3) not null,
    IN_PROGRESS_TIME_ datetime(3),
    IN_PROGRESS_STARTED_BY_ varchar(255),
    CLAIM_TIME_ datetime(3),
    CLAIMED_BY_ varchar(255),
    SUSPENDED_TIME_ datetime(3),
    SUSPENDED_BY_ varchar(255),
    END_TIME_ datetime(3),
    COMPLETED_BY_ varchar(255),
    DURATION_ bigint,
    DELETE_REASON_ varchar(4000),
    PRIORITY_ integer,
    IN_PROGRESS_DUE_DATE_ datetime(3),
    DUE_DATE_ datetime(3),
    FORM_KEY_ varchar(255),
    CATEGORY_ varchar(255),
    TENANT_ID_ varchar(255) default '',
    LAST_UPDATED_TIME_ datetime(3),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_HI_TSK_LOG (
    ID_ bigint auto_increment,
    TYPE_ varchar(64),
    TASK_ID_ varchar(64) not null,
    TIME_STAMP_ timestamp(3) not null,
    USER_ID_ varchar(255),
    DATA_ varchar(4000),
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    SCOPE_ID_ varchar(255),
    SCOPE_DEFINITION_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_HI_TASK_SCOPE on ACT_HI_TASKINST(SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_HI_TASK_SUB_SCOPE on ACT_HI_TASKINST(SUB_SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_HI_TASK_SCOPE_DEF on ACT_HI_TASKINST(SCOPE_DEFINITION_ID_, SCOPE_TYPE_);
create index ACT_IDX_ACT_HI_TSK_LOG_TASK on ACT_HI_TSK_LOG(TASK_ID_);


create table ACT_RU_VARIABLE (
    ID_ varchar(64) not null,
    REV_ integer,
    TYPE_ varchar(255) not null,
    NAME_ varchar(255) not null,
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    TASK_ID_ varchar(64),
    SCOPE_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    META_INFO_ varchar(4000),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_RU_VAR_SCOPE_ID_TYPE on ACT_RU_VARIABLE(SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_RU_VAR_SUB_ID_TYPE on ACT_RU_VARIABLE(SUB_SCOPE_ID_, SCOPE_TYPE_);

alter table ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_BYTEARRAY
    foreign key (BYTEARRAY_ID_)
    references ACT_GE_BYTEARRAY (ID_);

create table ACT_HI_VARINST (
    ID_ varchar(64) not null,
    REV_ integer default 1,
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(100),
    SCOPE_ID_ varchar(255),
    SUB_SCOPE_ID_ varchar(255),
    SCOPE_TYPE_ varchar(255),
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    META_INFO_ varchar(4000),
    CREATE_TIME_ datetime(3),
    LAST_UPDATED_TIME_ datetime(3),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_VARINST(NAME_, VAR_TYPE_);
create index ACT_IDX_HI_VAR_SCOPE_ID_TYPE on ACT_HI_VARINST(SCOPE_ID_, SCOPE_TYPE_);
create index ACT_IDX_HI_VAR_SUB_ID_TYPE on ACT_HI_VARINST(SUB_SCOPE_ID_, SCOPE_TYPE_);


create table ACT_RU_EVENT_SUBSCR (
    ID_ varchar(64) not null,
    REV_ integer,
    EVENT_TYPE_ varchar(255) not null,
    EVENT_NAME_ varchar(255),
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    ACTIVITY_ID_ varchar(64),
    CONFIGURATION_ varchar(255),
    CREATED_ timestamp(3) not null DEFAULT CURRENT_TIMESTAMP(3),
    PROC_DEF_ID_ varchar(64),
    SUB_SCOPE_ID_ varchar(64),
    SCOPE_ID_ varchar(64),
    SCOPE_DEFINITION_ID_ varchar(64),
    SCOPE_DEFINITION_KEY_ varchar(255),
    SCOPE_TYPE_ varchar(64),
    LOCK_TIME_ timestamp(3) NULL,
    LOCK_OWNER_ varchar(255),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_EVENT_SUBSCR_CONFIG_ on ACT_RU_EVENT_SUBSCR(CONFIGURATION_);
create index ACT_IDX_EVENT_SUBSCR_EXEC_ID on ACT_RU_EVENT_SUBSCR(EXECUTION_ID_);
create index ACT_IDX_EVENT_SUBSCR_PROC_ID on ACT_RU_EVENT_SUBSCR(PROC_INST_ID_);
create index ACT_IDX_EVENT_SUBSCR_SCOPEREF_ on ACT_RU_EVENT_SUBSCR(SCOPE_ID_, SCOPE_TYPE_);

create table ACT_RE_DEPLOYMENT (
    ID_ varchar(64),
    NAME_ varchar(255),
    CATEGORY_ varchar(255),
    KEY_ varchar(255),
    TENANT_ID_ varchar(255) default '',
    DEPLOY_TIME_ timestamp(3) NULL,
    DERIVED_FROM_ varchar(64),
    DERIVED_FROM_ROOT_ varchar(64),
    PARENT_DEPLOYMENT_ID_ varchar(255),
    ENGINE_VERSION_ varchar(255),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RE_MODEL (
    ID_ varchar(64) not null,
    REV_ integer,
    NAME_ varchar(255),
    KEY_ varchar(255),
    CATEGORY_ varchar(255),
    CREATE_TIME_ timestamp(3) null,
    LAST_UPDATE_TIME_ timestamp(3) null,
    VERSION_ integer,
    META_INFO_ varchar(4000),
    DEPLOYMENT_ID_ varchar(64),
    EDITOR_SOURCE_VALUE_ID_ varchar(64),
    EDITOR_SOURCE_EXTRA_VALUE_ID_ varchar(64),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_EXECUTION (
    ID_ varchar(64),
    REV_ integer,
    PROC_INST_ID_ varchar(64),
    BUSINESS_KEY_ varchar(255),
    PARENT_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    SUPER_EXEC_ varchar(64),
    ROOT_PROC_INST_ID_ varchar(64),
    ACT_ID_ varchar(255),
    IS_ACTIVE_ TINYINT,
    IS_CONCURRENT_ TINYINT,
    IS_SCOPE_ TINYINT,
    IS_EVENT_SCOPE_ TINYINT,
    IS_MI_ROOT_ TINYINT,
    SUSPENSION_STATE_ integer,
    CACHED_ENT_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    NAME_ varchar(255),
    START_ACT_ID_ varchar(255),
    START_TIME_ datetime(3),
    START_USER_ID_ varchar(255),
    LOCK_TIME_ timestamp(3) NULL,
    LOCK_OWNER_ varchar(255),
    IS_COUNT_ENABLED_ TINYINT,
    EVT_SUBSCR_COUNT_ integer,
    TASK_COUNT_ integer,
    JOB_COUNT_ integer,
    TIMER_JOB_COUNT_ integer,
    SUSP_JOB_COUNT_ integer,
    DEADLETTER_JOB_COUNT_ integer,
    EXTERNAL_WORKER_JOB_COUNT_ integer,
    VAR_COUNT_ integer,
    ID_LINK_COUNT_ integer,
    CALLBACK_ID_ varchar(255),
    CALLBACK_TYPE_ varchar(255),
    REFERENCE_ID_ varchar(255),
    REFERENCE_TYPE_ varchar(255),
    PROPAGATED_STAGE_INST_ID_ varchar(255),
    BUSINESS_STATUS_ varchar(255),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RE_PROCDEF (
    ID_ varchar(64) not null,
    REV_ integer,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255) not null,
    VERSION_ integer not null,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    DGRM_RESOURCE_NAME_ varchar(4000),
    DESCRIPTION_ varchar(4000),
    HAS_START_FORM_KEY_ TINYINT,
    HAS_GRAPHICAL_NOTATION_ TINYINT,
    SUSPENSION_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    ENGINE_VERSION_ varchar(255),
    DERIVED_FROM_ varchar(64),
    DERIVED_FROM_ROOT_ varchar(64),
    DERIVED_VERSION_ integer not null default 0,
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_EVT_LOG (
    LOG_NR_ bigint auto_increment,
    TYPE_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    TIME_STAMP_ timestamp(3) not null DEFAULT CURRENT_TIMESTAMP(3),
    USER_ID_ varchar(255),
    DATA_ LONGBLOB,
    LOCK_OWNER_ varchar(255),
    LOCK_TIME_ timestamp(3) null,
    IS_PROCESSED_ tinyint default 0,
    primary key (LOG_NR_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_PROCDEF_INFO (
	ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64) not null,
    REV_ integer,
    INFO_JSON_ID_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_ACTINST (
    ID_ varchar(64) not null,
    REV_ integer default 1,
    PROC_DEF_ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64) not null,
    EXECUTION_ID_ varchar(64) not null,
    ACT_ID_ varchar(255) not null,
    TASK_ID_ varchar(64),
    CALL_PROC_INST_ID_ varchar(64),
    ACT_NAME_ varchar(255),
    ACT_TYPE_ varchar(255) not null,
    ASSIGNEE_ varchar(255),
    COMPLETED_BY_ varchar(255),
    START_TIME_ datetime(3) not null,
    END_TIME_ datetime(3),
    DURATION_ bigint,
    TRANSACTION_ORDER_ integer,
    DELETE_REASON_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_EXEC_BUSKEY on ACT_RU_EXECUTION(BUSINESS_KEY_);
create index ACT_IDC_EXEC_ROOT on ACT_RU_EXECUTION(ROOT_PROC_INST_ID_);
create index ACT_IDX_EXEC_REF_ID_ on ACT_RU_EXECUTION(REFERENCE_ID_);
create index ACT_IDX_VARIABLE_TASK_ID on ACT_RU_VARIABLE(TASK_ID_);
create index ACT_IDX_ATHRZ_PROCEDEF on ACT_RU_IDENTITYLINK(PROC_DEF_ID_);
create index ACT_IDX_INFO_PROCDEF on ACT_PROCDEF_INFO(PROC_DEF_ID_);

create index ACT_IDX_BYTEAR_DEPL on ACT_GE_BYTEARRAY(DEPLOYMENT_ID_);

create index ACT_IDX_RU_ACTI_START on ACT_RU_ACTINST(START_TIME_);
create index ACT_IDX_RU_ACTI_END on ACT_RU_ACTINST(END_TIME_);
create index ACT_IDX_RU_ACTI_PROC on ACT_RU_ACTINST(PROC_INST_ID_);
create index ACT_IDX_RU_ACTI_PROC_ACT on ACT_RU_ACTINST(PROC_INST_ID_, ACT_ID_);
create index ACT_IDX_RU_ACTI_EXEC on ACT_RU_ACTINST(EXECUTION_ID_);
create index ACT_IDX_RU_ACTI_EXEC_ACT on ACT_RU_ACTINST(EXECUTION_ID_, ACT_ID_);
create index ACT_IDX_RU_ACTI_TASK on ACT_RU_ACTINST(TASK_ID_);

alter table ACT_GE_BYTEARRAY
    add constraint ACT_FK_BYTEARR_DEPL
    foreign key (DEPLOYMENT_ID_)
    references ACT_RE_DEPLOYMENT (ID_);

alter table ACT_RE_PROCDEF
    add constraint ACT_UNIQ_PROCDEF
    unique (KEY_,VERSION_, DERIVED_VERSION_, TENANT_ID_);

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION (ID_) on delete cascade on update cascade;

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PARENT
    foreign key (PARENT_ID_)
    references ACT_RU_EXECUTION (ID_) on delete cascade;

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_SUPER
    foreign key (SUPER_EXEC_)
    references ACT_RU_EXECUTION (ID_) on delete cascade;

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCDEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_TSKASS_TASK
    foreign key (TASK_ID_)
    references ACT_RU_TASK (ID_);

alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_ATHRZ_PROCEDEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF(ID_);

alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_IDL_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_TASK
    add constraint ACT_FK_TASK_EXE
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_TASK
    add constraint ACT_FK_TASK_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_TASK
    add constraint ACT_FK_TASK_PROCDEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

alter table ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_EXE
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION(ID_);

alter table ACT_RU_JOB
    add constraint ACT_FK_JOB_EXECUTION
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_JOB
    add constraint ACT_FK_JOB_PROCESS_INSTANCE
    foreign key (PROCESS_INSTANCE_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_JOB
    add constraint ACT_FK_JOB_PROC_DEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

alter table ACT_RU_TIMER_JOB
    add constraint ACT_FK_TIMER_JOB_EXECUTION
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_TIMER_JOB
    add constraint ACT_FK_TIMER_JOB_PROCESS_INSTANCE
    foreign key (PROCESS_INSTANCE_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_TIMER_JOB
    add constraint ACT_FK_TIMER_JOB_PROC_DEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

alter table ACT_RU_SUSPENDED_JOB
    add constraint ACT_FK_SUSPENDED_JOB_EXECUTION
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_SUSPENDED_JOB
    add constraint ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE
    foreign key (PROCESS_INSTANCE_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_SUSPENDED_JOB
    add constraint ACT_FK_SUSPENDED_JOB_PROC_DEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

alter table ACT_RU_DEADLETTER_JOB
    add constraint ACT_FK_DEADLETTER_JOB_EXECUTION
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_DEADLETTER_JOB
    add constraint ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE
    foreign key (PROCESS_INSTANCE_ID_)
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_DEADLETTER_JOB
    add constraint ACT_FK_DEADLETTER_JOB_PROC_DEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

alter table ACT_RU_EVENT_SUBSCR
    add constraint ACT_FK_EVENT_EXEC
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION(ID_);

alter table ACT_RE_MODEL
    add constraint ACT_FK_MODEL_SOURCE
    foreign key (EDITOR_SOURCE_VALUE_ID_)
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RE_MODEL
    add constraint ACT_FK_MODEL_SOURCE_EXTRA
    foreign key (EDITOR_SOURCE_EXTRA_VALUE_ID_)
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RE_MODEL
    add constraint ACT_FK_MODEL_DEPLOYMENT
    foreign key (DEPLOYMENT_ID_)
    references ACT_RE_DEPLOYMENT (ID_);

alter table ACT_PROCDEF_INFO
    add constraint ACT_FK_INFO_JSON_BA
    foreign key (INFO_JSON_ID_)
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_PROCDEF_INFO
    add constraint ACT_FK_INFO_PROCDEF
    foreign key (PROC_DEF_ID_)
    references ACT_RE_PROCDEF (ID_);

alter table ACT_PROCDEF_INFO
    add constraint ACT_UNIQ_INFO_PROCDEF
    unique (PROC_DEF_ID_);

insert into ACT_GE_PROPERTY
values ('schema.version', '8.0.0.0', 1);

insert into ACT_GE_PROPERTY
values ('schema.history', 'create(8.0.0.0)', 1);

create table ACT_HI_PROCINST (
    ID_ varchar(64) not null,
    REV_ integer default 1,
    PROC_INST_ID_ varchar(64) not null,
    BUSINESS_KEY_ varchar(255),
    PROC_DEF_ID_ varchar(64) not null,
    START_TIME_ datetime(3) not null,
    END_TIME_ datetime(3),
    DURATION_ bigint,
    START_USER_ID_ varchar(255),
    START_ACT_ID_ varchar(255),
    END_ACT_ID_ varchar(255),
    SUPER_PROCESS_INSTANCE_ID_ varchar(64),
    DELETE_REASON_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    NAME_ varchar(255),
    CALLBACK_ID_ varchar(255),
    CALLBACK_TYPE_ varchar(255),
    REFERENCE_ID_ varchar(255),
    REFERENCE_TYPE_ varchar(255),
    PROPAGATED_STAGE_INST_ID_ varchar(255),
    BUSINESS_STATUS_ varchar(255),
    END_USER_ID_ varchar(255),
    STATE_ varchar(255),
    primary key (ID_),
    unique (PROC_INST_ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_HI_ACTINST (
    ID_ varchar(64) not null,
    REV_ integer default 1,
    PROC_DEF_ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64) not null,
    EXECUTION_ID_ varchar(64) not null,
    ACT_ID_ varchar(255) not null,
    TASK_ID_ varchar(64),
    CALL_PROC_INST_ID_ varchar(64),
    ACT_NAME_ varchar(255),
    ACT_TYPE_ varchar(255) not null,
    ASSIGNEE_ varchar(255),
    COMPLETED_BY_ varchar(255),
    START_TIME_ datetime(3) not null,
    END_TIME_ datetime(3),
    TRANSACTION_ORDER_ integer,
    DURATION_ bigint,
    DELETE_REASON_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_HI_DETAIL (
    ID_ varchar(64) not null,
    TYPE_ varchar(255) not null,
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    ACT_INST_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(255),
    REV_ integer,
    TIME_ datetime(3) not null,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_HI_COMMENT (
    ID_ varchar(64) not null,
    TYPE_ varchar(255),
    TIME_ datetime(3) not null,
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    ACTION_ varchar(255),
    MESSAGE_ varchar(4000),
    FULL_MSG_ LONGBLOB,
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_HI_ATTACHMENT (
    ID_ varchar(64) not null,
    REV_ integer,
    USER_ID_ varchar(255),
    NAME_ varchar(255),
    DESCRIPTION_ varchar(4000),
    TYPE_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    URL_ varchar(4000),
    CONTENT_ID_ varchar(64),
    TIME_ datetime(3),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;


create index ACT_IDX_HI_PRO_INST_END on ACT_HI_PROCINST(END_TIME_);
create index ACT_IDX_HI_PRO_I_BUSKEY on ACT_HI_PROCINST(BUSINESS_KEY_);
create index ACT_IDX_HI_PRO_SUPER_PROCINST on ACT_HI_PROCINST(SUPER_PROCESS_INSTANCE_ID_);
create index ACT_IDX_HI_ACT_INST_START on ACT_HI_ACTINST(START_TIME_);
create index ACT_IDX_HI_ACT_INST_END on ACT_HI_ACTINST(END_TIME_);
create index ACT_IDX_HI_DETAIL_PROC_INST on ACT_HI_DETAIL(PROC_INST_ID_);
create index ACT_IDX_HI_DETAIL_ACT_INST on ACT_HI_DETAIL(ACT_INST_ID_);
create index ACT_IDX_HI_DETAIL_TIME on ACT_HI_DETAIL(TIME_);
create index ACT_IDX_HI_DETAIL_NAME on ACT_HI_DETAIL(NAME_);
create index ACT_IDX_HI_DETAIL_TASK_ID on ACT_HI_DETAIL(TASK_ID_);
create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_TASK_ID on ACT_HI_VARINST(TASK_ID_);
create index ACT_IDX_HI_PROCVAR_EXE on ACT_HI_VARINST(EXECUTION_ID_);
create index ACT_IDX_HI_ACT_INST_PROCINST on ACT_HI_ACTINST(PROC_INST_ID_, ACT_ID_);
create index ACT_IDX_HI_ACT_INST_EXEC on ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_);
create index ACT_IDX_HI_IDENT_LNK_TASK on ACT_HI_IDENTITYLINK(TASK_ID_);
create index ACT_IDX_HI_IDENT_LNK_PROCINST on ACT_HI_IDENTITYLINK(PROC_INST_ID_);
create index ACT_IDX_HI_TASK_INST_PROCINST on ACT_HI_TASKINST(PROC_INST_ID_);

-- ===== Jeecg workflow business schema and menu/permission increments =====

-- ----- V20260904_1__workflow_foundation.sql -----
-- Workflow foundation adapted from ruoyi-vue-pro/sql/mysql/bpm-2026-04-18.sql.
-- Only reusable metadata is migrated. Demo data and bpm_oa_leave are intentionally excluded.

CREATE TABLE IF NOT EXISTS `bpm_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `code` varchar(64) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `sort` int NOT NULL DEFAULT 0,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bpm_category_tenant_code` (`tenant_id`, `code`),
  KEY `idx_bpm_category_tenant_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流分类';

CREATE TABLE IF NOT EXISTS `bpm_form` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `conf` longtext NOT NULL,
  `fields` longtext NOT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_bpm_form_tenant_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流表单配置';

CREATE TABLE IF NOT EXISTS `bpm_process_definition_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `process_definition_id` varchar(64) NOT NULL,
  `model_id` varchar(64) DEFAULT NULL,
  `model_type` tinyint NOT NULL DEFAULT 10,
  `category` varchar(64) DEFAULT NULL,
  `icon` varchar(512) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `form_type` tinyint NOT NULL DEFAULT 20,
  `form_id` bigint DEFAULT NULL,
  `form_conf` longtext,
  `form_fields` longtext,
  `form_custom_create_path` varchar(255) DEFAULT NULL,
  `form_custom_view_path` varchar(255) DEFAULT NULL,
  `simple_model` longtext,
  `sort` bigint NOT NULL DEFAULT 0,
  `visible` tinyint NOT NULL DEFAULT 1,
  `start_user_ids` longtext,
  `start_dept_ids` longtext,
  `manager_user_ids` longtext,
  `allow_cancel_running_process` tinyint NOT NULL DEFAULT 1,
  `allow_withdraw_task` tinyint NOT NULL DEFAULT 0,
  `process_id_rule` varchar(255) DEFAULT NULL,
  `auto_approval_type` tinyint NOT NULL DEFAULT 0,
  `title_setting` varchar(512) DEFAULT NULL,
  `summary_setting` varchar(1024) DEFAULT NULL,
  `print_template_setting` longtext,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bpm_definition_info_definition` (`process_definition_id`),
  KEY `idx_bpm_definition_info_tenant_category` (`tenant_id`, `category`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流流程定义扩展';

CREATE TABLE IF NOT EXISTS `bpm_node_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `process_definition_id` varchar(64) NOT NULL,
  `node_key` varchar(128) NOT NULL,
  `node_name` varchar(255) DEFAULT NULL,
  `form_view_url` varchar(255) DEFAULT NULL,
  `form_edit_enabled` tinyint NOT NULL DEFAULT 0,
  `field_permission_json` longtext,
  `button_permission_json` longtext,
  `node_extension_json` longtext,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bpm_node_definition_key` (`process_definition_id`, `node_key`),
  KEY `idx_bpm_node_tenant` (`tenant_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流节点扩展配置';

CREATE TABLE IF NOT EXISTS `bpm_process_expression` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `expression` varchar(1024) NOT NULL,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_bpm_expression_tenant_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流表达式';

CREATE TABLE IF NOT EXISTS `bpm_process_listener` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `type` varchar(64) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `event` varchar(64) NOT NULL,
  `value_type` varchar(64) NOT NULL,
  `value` varchar(1024) NOT NULL,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_bpm_listener_tenant_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流监听器配置';

CREATE TABLE IF NOT EXISTS `bpm_user_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `user_ids` longtext,
  `status` tinyint NOT NULL DEFAULT 1,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_bpm_user_group_tenant_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流用户组';

CREATE TABLE IF NOT EXISTS `bpm_process_instance_copy` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(36) NOT NULL,
  `start_user_id` varchar(36) NOT NULL,
  `process_instance_id` varchar(64) NOT NULL,
  `process_instance_name` varchar(255) NOT NULL,
  `process_definition_id` varchar(64) NOT NULL,
  `category` varchar(64) DEFAULT NULL,
  `activity_id` varchar(64) DEFAULT NULL,
  `activity_name` varchar(255) DEFAULT NULL,
  `task_id` varchar(64) DEFAULT NULL,
  `reason` varchar(512) DEFAULT NULL,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_bpm_copy_user` (`tenant_id`, `user_id`, `deleted`),
  KEY `idx_bpm_copy_process` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流抄送记录';

-- Menus and permissions. IDs are stable so upgrades remain idempotent.
INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000001', NULL, '工作流', '/workflow', 'layouts/default/index', 1, 'workflow', '/workflow/task/todo', 0, NULL, '1', 30, 1, 'ant-design:deployment-unit-outlined', 0, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000002', '1909040000000000001', '流程定义', '/workflow/definition', 'workflow/definition/index', 1, 'workflow-definition', NULL, 1, 'workflow:definition:list', '1', 1, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000003', '1909040000000000001', '我的待办', '/workflow/task/todo', 'workflow/task/todo', 1, 'workflow-task-todo', NULL, 1, 'workflow:task:list', '1', 2, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000004', '1909040000000000001', '我的已办', '/workflow/task/done', 'workflow/task/done', 1, 'workflow-task-done', NULL, 1, 'workflow:task:list', '1', 3, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000005', '1909040000000000001', '我的申请', '/workflow/process/my', 'workflow/process/my', 1, 'workflow-process-my', NULL, 1, 'workflow:process:list', '1', 4, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000006', '1909040000000000001', '流程实例', '/workflow/process/manager', 'workflow/process/manager', 1, 'workflow-process-manager', NULL, 1, 'workflow:process:manager', '1', 5, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000011', '1909040000000000002', '部署流程', NULL, NULL, 0, NULL, NULL, 2, 'workflow:definition:deploy', '1', 1, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000012', '1909040000000000003', '处理任务', NULL, NULL, 0, NULL, NULL, 2, 'workflow:task:handle', '1', 1, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000013', '1909040000000000005', '发起流程', NULL, NULL, 0, NULL, NULL, 2, 'workflow:process:start', '1', 1, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000014', '1909040000000000005', '取消流程', NULL, NULL, 0, NULL, NULL, 2, 'workflow:process:cancel', '1', 2, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`), `url` = VALUES(`url`), `component` = VALUES(`component`),
`perms` = VALUES(`perms`), `del_flag` = 0, `status` = '1';

-- ----- V20260904_2__workflow_menu_children.sql -----
-- Fix Vue3 menu rendering: always_show=1 suppresses child menus in the
-- current JeecgBoot menu component. The workflow root must expose children.
UPDATE `sys_permission`
SET `always_show` = 0,
    `is_leaf` = 0,
    `redirect` = '/workflow/task/todo',
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000001';

-- ----- V20260904_3__workflow_management_metadata.sql -----
-- Align the workflow management menu with the RuoYi/Yudao BPM hierarchy.
-- This migration only exposes pages whose Jeecg adapters are implemented.

ALTER TABLE `bpm_category` MODIFY `status` tinyint NOT NULL DEFAULT 0;
ALTER TABLE `bpm_form` MODIFY `status` tinyint NOT NULL DEFAULT 0;
ALTER TABLE `bpm_process_expression` MODIFY `status` tinyint NOT NULL DEFAULT 0;
ALTER TABLE `bpm_process_listener` MODIFY `status` tinyint NOT NULL DEFAULT 0;
ALTER TABLE `bpm_user_group` MODIFY `status` tinyint NOT NULL DEFAULT 0;

UPDATE `sys_permission`
SET `name` = '工作流程',
    `url` = '/bpm',
    `redirect` = '/bpm/task/todo',
    `component` = 'layouts/default/index',
    `always_show` = 0,
    `is_leaf` = 0,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000001';

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000020', '1909040000000000001', '流程管理', '/bpm/manager', 'layouts/RouteView', 1, 'bpm-manager', '/bpm/manager/definition', 0, NULL, '1', 10, 0, 'ant-design:control-outlined', 0, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000021', '1909040000000000001', '审批中心', '/bpm/task', 'layouts/RouteView', 1, 'bpm-task-center', '/bpm/task/create', 0, NULL, '1', 20, 0, 'ant-design:audit-outlined', 0, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000030', '1909040000000000020', '流程表单', '/bpm/manager/form', 'bpm/form/index', 1, 'BpmForm', NULL, 1, NULL, '1', 2, 0, 'ant-design:form-outlined', 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000031', '1909040000000000020', '流程分类', '/bpm/manager/category', 'bpm/category/index', 1, 'BpmCategory', NULL, 1, NULL, '1', 3, 0, 'ant-design:group-outlined', 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000032', '1909040000000000020', '用户分组', '/bpm/manager/user-group', 'bpm/group/index', 1, 'BpmUserGroup', NULL, 1, NULL, '1', 4, 0, 'ant-design:team-outlined', 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000033', '1909040000000000020', '流程监听器', '/bpm/manager/process-listener', 'bpm/processListener/index', 1, 'BpmProcessListener', NULL, 1, NULL, '1', 5, 0, 'ant-design:customer-service-outlined', 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000034', '1909040000000000020', '流程表达式', '/bpm/manager/process-expression', 'bpm/processExpression/index', 1, 'BpmProcessExpression', NULL, 1, NULL, '1', 6, 0, 'ant-design:function-outlined', 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `url` = VALUES(`url`),
`component` = VALUES(`component`), `component_name` = VALUES(`component_name`),
`sort_no` = VALUES(`sort_no`), `is_leaf` = VALUES(`is_leaf`), `del_flag` = 0, `status` = '1';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000020',
    `name` = '流程定义',
    `url` = '/bpm/manager/definition',
    `component` = 'workflow/definition/index',
    `sort_no` = 1,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000002';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000020',
    `name` = '流程实例',
    `url` = '/bpm/manager/process-instance',
    `component` = 'workflow/process/manager',
    `sort_no` = 10,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000006';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000021',
    `name` = '发起流程',
    `url` = '/bpm/task/create',
    `component` = 'workflow/process/my',
    `sort_no` = 0,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000005';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000021',
    `name` = '待办任务',
    `url` = '/bpm/task/todo',
    `component` = 'workflow/task/todo',
    `sort_no` = 10,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000003';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000021',
    `name` = '已办任务',
    `url` = '/bpm/task/done',
    `component` = 'workflow/task/done',
    `sort_no` = 20,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000004';

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `is_route`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000040', '1909040000000000030', '表单查询', 0, 2, 'bpm:form:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000041', '1909040000000000030', '表单创建', 0, 2, 'bpm:form:create', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000042', '1909040000000000030', '表单更新', 0, 2, 'bpm:form:update', '1', 3, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000043', '1909040000000000030', '表单删除', 0, 2, 'bpm:form:delete', '1', 4, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000044', '1909040000000000031', '分类查询', 0, 2, 'bpm:category:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000045', '1909040000000000031', '分类创建', 0, 2, 'bpm:category:create', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000046', '1909040000000000031', '分类更新', 0, 2, 'bpm:category:update', '1', 3, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000047', '1909040000000000031', '分类删除', 0, 2, 'bpm:category:delete', '1', 4, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000048', '1909040000000000032', '用户组查询', 0, 2, 'bpm:user-group:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000049', '1909040000000000032', '用户组创建', 0, 2, 'bpm:user-group:create', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000050', '1909040000000000032', '用户组更新', 0, 2, 'bpm:user-group:update', '1', 3, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000051', '1909040000000000032', '用户组删除', 0, 2, 'bpm:user-group:delete', '1', 4, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000052', '1909040000000000033', '监听器查询', 0, 2, 'bpm:process-listener:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000053', '1909040000000000033', '监听器创建', 0, 2, 'bpm:process-listener:create', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000054', '1909040000000000033', '监听器更新', 0, 2, 'bpm:process-listener:update', '1', 3, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000055', '1909040000000000033', '监听器删除', 0, 2, 'bpm:process-listener:delete', '1', 4, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000056', '1909040000000000034', '表达式查询', 0, 2, 'bpm:process-expression:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000057', '1909040000000000034', '表达式创建', 0, 2, 'bpm:process-expression:create', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000058', '1909040000000000034', '表达式更新', 0, 2, 'bpm:process-expression:update', '1', 3, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000059', '1909040000000000034', '表达式删除', 0, 2, 'bpm:process-expression:delete', '1', 4, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `perms` = VALUES(`perms`),
`sort_no` = VALUES(`sort_no`), `del_flag` = 0, `status` = '1';

-- ----- V20260904_4__workflow_role_permission_inheritance.sql -----
-- Keep existing workflow roles usable after introducing the RuoYi/Vben-style menu hierarchy.
-- Parent menu grants are derived from the pages a role already owned.

INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(existing_role.`role_id`, ':1909040000000000001')),
       existing_role.`role_id`,
       '1909040000000000001',
       NULL,
       NOW(),
       '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id`
    FROM `sys_role_permission`
    WHERE `permission_id` IN (
        '1909040000000000002', '1909040000000000003', '1909040000000000004',
        '1909040000000000005', '1909040000000000006'
    )
) existing_role
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = existing_role.`role_id`
      AND current_grant.`permission_id` = '1909040000000000001'
);

INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(existing_role.`role_id`, ':1909040000000000020')),
       existing_role.`role_id`,
       '1909040000000000020',
       NULL,
       NOW(),
       '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id`
    FROM `sys_role_permission`
    WHERE `permission_id` IN ('1909040000000000002', '1909040000000000006')
) existing_role
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = existing_role.`role_id`
      AND current_grant.`permission_id` = '1909040000000000020'
);

INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(existing_role.`role_id`, ':1909040000000000021')),
       existing_role.`role_id`,
       '1909040000000000021',
       NULL,
       NOW(),
       '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id`
    FROM `sys_role_permission`
    WHERE `permission_id` IN (
        '1909040000000000003', '1909040000000000004', '1909040000000000005'
    )
) existing_role
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = existing_role.`role_id`
      AND current_grant.`permission_id` = '1909040000000000021'
);

-- A role that owned all five legacy workflow pages is treated as a full workflow role.
-- It inherits the newly introduced management pages and their button permissions.
INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(full_workflow_role.`role_id`, ':', target_permission.`id`)),
       full_workflow_role.`role_id`,
       target_permission.`id`,
       NULL,
       NOW(),
       '127.0.0.1'
FROM (
    SELECT `role_id`
    FROM `sys_role_permission`
    WHERE `permission_id` IN (
        '1909040000000000002', '1909040000000000003', '1909040000000000004',
        '1909040000000000005', '1909040000000000006'
    )
    GROUP BY `role_id`
    HAVING COUNT(DISTINCT `permission_id`) = 5
) full_workflow_role
JOIN `sys_permission` target_permission
  ON target_permission.`id` IN (
      '1909040000000000011', '1909040000000000012', '1909040000000000013',
      '1909040000000000014', '1909040000000000030', '1909040000000000031',
      '1909040000000000032', '1909040000000000033', '1909040000000000034',
      '1909040000000000040', '1909040000000000041', '1909040000000000042',
      '1909040000000000043', '1909040000000000044', '1909040000000000045',
      '1909040000000000046', '1909040000000000047', '1909040000000000048',
      '1909040000000000049', '1909040000000000050', '1909040000000000051',
      '1909040000000000052', '1909040000000000053', '1909040000000000054',
      '1909040000000000055', '1909040000000000056', '1909040000000000057',
      '1909040000000000058', '1909040000000000059'
  )
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = full_workflow_role.`role_id`
      AND current_grant.`permission_id` = target_permission.`id`
);

-- ----- V20260904_5__workflow_model_menu.sql -----
-- Add the RuoYi/Vben workflow model entry. The old direct-deployment page remains as a hidden compatibility route.

UPDATE `sys_permission`
SET `redirect` = '/bpm/manager/model'
WHERE `id` = '1909040000000000020';

UPDATE `sys_permission`
SET `hidden` = 1,
    `sort_no` = 99
WHERE `id` = '1909040000000000002';

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000035', '1909040000000000020', '流程模型', '/bpm/manager/model', 'bpm/model/index', 1, 'BpmModel', NULL, 1, NULL, '1', 1, 0, 'ant-design:deployment-unit-outlined', 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `url` = VALUES(`url`),
`component` = VALUES(`component`), `component_name` = VALUES(`component_name`),
`sort_no` = VALUES(`sort_no`), `is_leaf` = VALUES(`is_leaf`), `hidden` = 0,
`del_flag` = 0, `status` = '1';

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `is_route`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000060', '1909040000000000035', '模型查询', 0, 2, 'bpm:model:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000061', '1909040000000000035', '模型创建', 0, 2, 'bpm:model:create', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000062', '1909040000000000035', '模型更新', 0, 2, 'bpm:model:update', '1', 3, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000063', '1909040000000000035', '模型删除', 0, 2, 'bpm:model:delete', '1', 4, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000064', '1909040000000000035', '模型发布', 0, 2, 'bpm:model:deploy', '1', 5, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000065', '1909040000000000035', '模型导入', 0, 2, 'bpm:model:import', '1', 6, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000066', '1909040000000000035', '模型导出', 0, 2, 'bpm:model:export', '1', 7, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000067', '1909040000000000035', '模型清理', 0, 2, 'bpm:model:clean', '1', 8, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `perms` = VALUES(`perms`),
`sort_no` = VALUES(`sort_no`), `del_flag` = 0, `status` = '1';

INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(full_workflow_role.`role_id`, ':', target_permission.`id`)),
       full_workflow_role.`role_id`,
       target_permission.`id`,
       NULL,
       NOW(),
       '127.0.0.1'
FROM (
    SELECT `role_id`
    FROM `sys_role_permission`
    WHERE `permission_id` IN (
        '1909040000000000002', '1909040000000000003', '1909040000000000004',
        '1909040000000000005', '1909040000000000006'
    )
    GROUP BY `role_id`
    HAVING COUNT(DISTINCT `permission_id`) = 5
) full_workflow_role
JOIN `sys_permission` target_permission
  ON target_permission.`id` IN (
      '1909040000000000035', '1909040000000000060', '1909040000000000061',
      '1909040000000000062', '1909040000000000063', '1909040000000000064',
      '1909040000000000065', '1909040000000000066', '1909040000000000067'
  )
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = full_workflow_role.`role_id`
      AND current_grant.`permission_id` = target_permission.`id`
);

-- ----- V20260904_6__workflow_task_center_alignment.sql -----
-- Complete the RuoYi/Yudao workflow menu hierarchy and add process copy storage.

CREATE TABLE IF NOT EXISTS `bpm_process_instance_copy` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` varchar(64) NOT NULL COMMENT '被抄送用户编号',
    `start_user_id` varchar(64) NOT NULL COMMENT '流程发起用户编号',
    `process_instance_id` varchar(64) NOT NULL COMMENT '流程实例编号',
    `process_instance_name` varchar(128) NOT NULL COMMENT '流程实例名称',
    `process_definition_id` varchar(128) NOT NULL COMMENT '流程定义编号',
    `category` varchar(64) DEFAULT NULL COMMENT '流程分类编码',
    `activity_id` varchar(128) DEFAULT NULL COMMENT '流程活动编号',
    `activity_name` varchar(128) DEFAULT NULL COMMENT '流程活动名称',
    `task_id` varchar(64) DEFAULT NULL COMMENT '任务编号',
    `reason` varchar(512) DEFAULT NULL COMMENT '抄送意见',
    `creator` varchar(64) DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT NULL,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` tinyint NOT NULL DEFAULT 0,
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_bpm_copy_user_tenant` (`user_id`, `tenant_id`, `deleted`),
    KEY `idx_bpm_copy_instance` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BPM 流程实例抄送表';

UPDATE `sys_permission`
SET `redirect` = '/bpm/task/create'
WHERE `id` = '1909040000000000001';

UPDATE `sys_permission`
SET `redirect` = '/bpm/manager/model'
WHERE `id` = '1909040000000000020';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000020',
    `name` = '流程实例',
    `url` = '/bpm/manager/process-instance',
    `component` = 'bpm/processInstance/manager/index',
    `component_name` = 'BpmProcessInstanceManager',
    `perms` = NULL,
    `sort_no` = 10,
    `hidden` = 0,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000006';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000021',
    `name` = '发起流程',
    `url` = '/bpm/task/create',
    `component` = 'bpm/processInstance/create/index',
    `component_name` = 'BpmProcessInstanceCreate',
    `perms` = NULL,
    `sort_no` = 0,
    `keep_alive` = 0,
    `hidden` = 0,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000005';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000021',
    `name` = '待办任务',
    `url` = '/bpm/task/todo',
    `component` = 'bpm/task/todo/index',
    `component_name` = 'BpmTodoTask',
    `perms` = NULL,
    `sort_no` = 10,
    `hidden` = 0,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000003';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000021',
    `name` = '已办任务',
    `url` = '/bpm/task/done',
    `component` = 'bpm/task/done/index',
    `component_name` = 'BpmDoneTask',
    `perms` = NULL,
    `sort_no` = 20,
    `hidden` = 0,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000004';

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000036', '1909040000000000021', '我的流程', '/bpm/task/my', 'bpm/processInstance/index', 1, 'BpmProcessInstanceMy', NULL, 1, NULL, '1', 1, 0, 'ant-design:book-outlined', 1, 1, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000037', '1909040000000000020', '流程任务', '/bpm/manager/process-task', 'bpm/task/manager/index', 1, 'BpmManagerTask', NULL, 1, NULL, '1', 11, 0, 'ant-design:tags-outlined', 1, 1, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000038', '1909040000000000021', '抄送我的', '/bpm/task/copy', 'bpm/task/copy/index', 1, 'BpmProcessInstanceCopy', NULL, 1, 'bpm:process-instance-cc:query', '1', 30, 0, 'ant-design:copy-outlined', 1, 1, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `url` = VALUES(`url`),
`component` = VALUES(`component`), `component_name` = VALUES(`component_name`),
`perms` = VALUES(`perms`), `sort_no` = VALUES(`sort_no`), `is_leaf` = VALUES(`is_leaf`),
`keep_alive` = VALUES(`keep_alive`), `hidden` = 0, `del_flag` = 0, `status` = '1';

-- Reuse legacy button records so existing role associations keep working.
UPDATE `sys_permission`
SET `parent_id` = '1909040000000000036',
    `name` = '流程实例的创建',
    `perms` = 'bpm:process-instance:create',
    `sort_no` = 2,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000013';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000036',
    `name` = '流程实例的取消',
    `perms` = 'bpm:process-instance:cancel',
    `sort_no` = 3,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000014';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000003',
    `name` = '流程任务的更新',
    `perms` = 'bpm:task:update',
    `sort_no` = 2,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000012';

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `is_route`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000068', '1909040000000000036', '流程实例的查询', 0, 2, 'bpm:process-instance:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000069', '1909040000000000035', '流程定义查询', 0, 2, 'bpm:process-definition:query', '1', 10, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000070', '1909040000000000003', '流程任务的查询', 0, 2, 'bpm:task:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000072', '1909040000000000006', '流程实例的查询（管理员）', 0, 2, 'bpm:process-instance:manager-query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000073', '1909040000000000037', '流程任务的查询（管理员）', 0, 2, 'bpm:task:manager-query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000074', '1909040000000000006', '流程实例的取消（管理员）', 0, 2, 'bpm:process-instance:cancel-by-admin', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `perms` = VALUES(`perms`),
`sort_no` = VALUES(`sort_no`), `del_flag` = 0, `status` = '1';

-- The former combined "我的申请" role receives the two split approval pages and their actions.
INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(source_role.`role_id`, ':', target_permission.`id`)), source_role.`role_id`, target_permission.`id`, NULL, NOW(), '127.0.0.1'
FROM (SELECT DISTINCT `role_id` FROM `sys_role_permission` WHERE `permission_id` = '1909040000000000005') source_role
JOIN `sys_permission` target_permission
  ON target_permission.`id` IN ('1909040000000000036', '1909040000000000068', '1909040000000000013', '1909040000000000014', '1909040000000000069')
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = source_role.`role_id` AND current_grant.`permission_id` = target_permission.`id`
);

-- Todo and done users receive target-compatible task permissions and the copy inbox.
INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(source_role.`role_id`, ':', target_permission.`id`)), source_role.`role_id`, target_permission.`id`, NULL, NOW(), '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id` FROM `sys_role_permission`
    WHERE `permission_id` IN ('1909040000000000003', '1909040000000000004', '1909040000000000005')
) source_role
JOIN `sys_permission` target_permission
  ON target_permission.`id` IN ('1909040000000000070', '1909040000000000012', '1909040000000000038')
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = source_role.`role_id` AND current_grant.`permission_id` = target_permission.`id`
);

-- Existing process administrators also receive the target manager pages and actions.
INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(source_role.`role_id`, ':', target_permission.`id`)), source_role.`role_id`, target_permission.`id`, NULL, NOW(), '127.0.0.1'
FROM (SELECT DISTINCT `role_id` FROM `sys_role_permission` WHERE `permission_id` = '1909040000000000006') source_role
JOIN `sys_permission` target_permission
  ON target_permission.`id` IN ('1909040000000000037', '1909040000000000072', '1909040000000000073', '1909040000000000074')
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = source_role.`role_id` AND current_grant.`permission_id` = target_permission.`id`
);

-- Keep parent folders visible for every role that owns one of the aligned workflow leaves.
INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(source_role.`role_id`, ':', parent_permission.`id`)), source_role.`role_id`, parent_permission.`id`, NULL, NOW(), '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id` FROM `sys_role_permission`
    WHERE `permission_id` IN (
        '1909040000000000035', '1909040000000000030', '1909040000000000031', '1909040000000000032',
        '1909040000000000033', '1909040000000000034', '1909040000000000006', '1909040000000000037',
        '1909040000000000005', '1909040000000000036', '1909040000000000003', '1909040000000000004', '1909040000000000038'
    )
) source_role
JOIN `sys_permission` parent_permission
  ON parent_permission.`id` IN ('1909040000000000001', '1909040000000000020', '1909040000000000021')
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = source_role.`role_id` AND current_grant.`permission_id` = parent_permission.`id`
);

-- ----- V20260904_7__workflow_process_detail_route.sql -----
-- Add the hidden process detail route shared by todo, done, copied and manager lists.

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000039', '1909040000000000001', '流程详情', '/bpm/process-instance/detail', 'bpm/processInstance/detail/index', 1, 'BpmProcessInstanceDetail', NULL, 1, NULL, '1', 99, 0, NULL, 1, 0, 1, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `url` = VALUES(`url`),
`component` = VALUES(`component`), `component_name` = VALUES(`component_name`),
`is_route` = 1, `menu_type` = 1, `is_leaf` = 1, `keep_alive` = 0,
`hidden` = 1, `del_flag` = 0, `status` = '1';

INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(source_role.`role_id`, ':1909040000000000039')), source_role.`role_id`, '1909040000000000039', NULL, NOW(), '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id` FROM `sys_role_permission`
    WHERE `permission_id` IN (
        '1909040000000000003', '1909040000000000004', '1909040000000000005',
        '1909040000000000006', '1909040000000000036', '1909040000000000037',
        '1909040000000000038'
    )
) source_role
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = source_role.`role_id`
      AND current_grant.`permission_id` = '1909040000000000039'
);

-- ----- V20260910_1__workflow_process_instance_report_route.sql -----
-- Add the hidden data-report route opened from a published process model.

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909100000000000001', '1909040000000000001', '数据报表', '/bpm/process-instance/report', 'bpm/processInstance/report/index', 1, 'BpmProcessInstanceReport', NULL, 1, NULL, '1', 98, 0, 'ant-design:bar-chart-outlined', 1, 0, 1, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `url` = VALUES(`url`),
`component` = VALUES(`component`), `component_name` = VALUES(`component_name`),
`is_route` = 1, `menu_type` = 1, `is_leaf` = 1, `keep_alive` = 0,
`hidden` = 1, `del_flag` = 0, `status` = '1';

-- Only roles that already have process-instance manager access receive the hidden route.
INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(source_role.`role_id`, ':1909100000000000001')), source_role.`role_id`, '1909100000000000001', NULL, NOW(), '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id`
    FROM `sys_role_permission`
    WHERE `permission_id` IN ('1909040000000000006', '1909040000000000072')
) source_role
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = source_role.`role_id`
      AND current_grant.`permission_id` = '1909100000000000001'
);

SET FOREIGN_KEY_CHECKS = 1;
-- End of workflow installation package.
