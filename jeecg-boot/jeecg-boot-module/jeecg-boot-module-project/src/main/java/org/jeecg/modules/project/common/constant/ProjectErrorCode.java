package org.jeecg.modules.project.common.constant;

/**
 * @Description: 项目模块业务异常码
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
public final class ProjectErrorCode {

    private ProjectErrorCode() {
    }

    /** 业务对象不存在 */
    public static final String BIZ_NOT_FOUND = "MRP-1001";
    /** 参数校验失败 */
    public static final String PARAM_INVALID = "MRP-1002";
    /** 当前状态不允许该操作 */
    public static final String STATUS_NOT_ALLOWED = "MRP-1003";
    /** 流程部署失败 */
    public static final String PROCESS_DEPLOY_FAILED = "MRP-2001";
    /** 外部系统调用失败 */
    public static final String INTEGRATION_FAILED = "MRP-3001";
}
