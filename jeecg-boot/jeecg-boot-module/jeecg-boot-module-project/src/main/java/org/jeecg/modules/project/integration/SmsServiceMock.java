package org.jeecg.modules.project.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description: 短信服务 Mock 实现（二期接入真实短信网关）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Slf4j
@Service("smsServiceMock")
public class SmsServiceMock implements SmsService {

    @Override
    public void sendSms(String phone, String content) {
        log.info("[mock-sms] to={}, content={}", phone, content);
    }
}
