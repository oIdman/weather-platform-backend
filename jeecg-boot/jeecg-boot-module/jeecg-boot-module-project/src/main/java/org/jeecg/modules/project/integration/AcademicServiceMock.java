package org.jeecg.modules.project.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @Description: 学术库服务 Mock 实现（二期对接知网 / 万方 / Web of Science）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Slf4j
@Service("academicServiceMock")
public class AcademicServiceMock implements AcademicService {

    @Override
    public List<String> searchPapers(String keyword, int limit) {
        log.info("[mock-academic] searchPapers keyword={}, limit={}", keyword, limit);
        return Collections.emptyList();
    }
}
