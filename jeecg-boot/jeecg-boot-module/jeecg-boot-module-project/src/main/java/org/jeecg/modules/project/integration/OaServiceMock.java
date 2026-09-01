package org.jeecg.modules.project.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description: OA 服务 Mock 实现（二期对接真实 OA 系统）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Slf4j
@Service("oaServiceMock")
public class OaServiceMock implements OaService {

    @Override
    public void pushTodo(String userId, String title, String url) {
        log.info("[mock-oa] pushTodo userId={}, title={}, url={}", userId, title, url);
    }

    @Override
    public void sendMessage(String userId, String content) {
        log.info("[mock-oa] sendMessage userId={}, content={}", userId, content);
    }
}
