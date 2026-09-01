package org.jeecg.modules.project.common.exception;

import org.jeecg.common.exception.JeecgBootException;

/**
 * @Description: 项目模块业务异常
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
public class ProjectBizException extends JeecgBootException {

    private static final long serialVersionUID = 1L;

    public ProjectBizException(String message) {
        super(message);
    }

    public ProjectBizException(String message, Throwable cause) {
        super(message, cause);
    }
}
