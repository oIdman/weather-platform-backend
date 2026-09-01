package org.jeecg.modules.project.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description: 档案服务 Mock 实现（二期对接真实数字档案系统）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Slf4j
@Service("archiveServiceMock")
public class ArchiveServiceMock implements ArchiveService {

    @Override
    public void pushArchive(String archivePackageJson) {
        log.info("[mock-archive] pushArchive package={}", archivePackageJson);
    }
}
