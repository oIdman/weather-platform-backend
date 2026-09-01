package org.jeecg.modules.project.integration;

/**
 * @Description: 数字档案系统服务（外部对接接口，本期 Mock）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
public interface ArchiveService {

    /**
     * 推送归档电子档案包
     *
     * @param archivePackageJson 归档包（JSON）
     */
    void pushArchive(String archivePackageJson);
}
