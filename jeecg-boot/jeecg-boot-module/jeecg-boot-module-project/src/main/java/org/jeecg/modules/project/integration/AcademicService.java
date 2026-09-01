package org.jeecg.modules.project.integration;

import java.util.List;

/**
 * @Description: 学术数据库服务（外部对接接口，本期 Mock）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
public interface AcademicService {

    /**
     * 检索论文
     *
     * @param keyword 关键词
     * @param limit   返回条数
     * @return 论文标题列表
     */
    List<String> searchPapers(String keyword, int limit);
}
