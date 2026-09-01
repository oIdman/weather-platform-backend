package org.jeecg.modules.project.integration;

/**
 * @Description: OA 办公协同服务（外部对接接口，本期 Mock）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
public interface OaService {

    /**
     * 推送待办
     *
     * @param userId 用户ID
     * @param title  待办标题
     * @param url    跳转地址
     */
    void pushTodo(String userId, String title, String url);

    /**
     * 发送 OA 站内消息
     *
     * @param userId  接收人ID
     * @param content 消息内容
     */
    void sendMessage(String userId, String content);
}
