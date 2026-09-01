package org.jeecg.modules.project.integration;

/**
 * @Description: 短信服务（外部对接接口，本期 Mock）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
public interface SmsService {

    /**
     * 发送短信
     *
     * @param phone   手机号
     * @param content 短信内容
     */
    void sendSms(String phone, String content);
}
