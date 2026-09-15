package org.jeecg.modules.workflow.starter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.config.RestTemplateConfig;
import org.jeecg.modules.workflow.adapter.WorkflowCandidateIdentityAdapter;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.adapter.jeecg.JeecgWorkflowCandidateIdentityAdapter;
import org.jeecg.modules.workflow.adapter.jeecg.JeecgWorkflowIdentityAdapter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 独立工作流服务启动器。
 *
 * <p>通过排除 Jeecg 本地身份适配器，使工作流核心可以在没有 system-biz 的服务中启动；
 * 业务系统应以 Bean 形式替换默认身份和候选人适配器。</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(WorkflowStandaloneProperties.class)
@MapperScan("org.jeecg.modules.workflow.mapper")
@Import(RestTemplateConfig.class)
@ComponentScan(
        basePackages = "org.jeecg.modules.workflow",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {JeecgWorkflowIdentityAdapter.class, JeecgWorkflowCandidateIdentityAdapter.class}))
public class WorkflowStandaloneApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowStandaloneApplication.class, args);
    }

    @Bean
    @ConditionalOnMissingBean({WorkflowIdentityAdapter.class, WorkflowCandidateIdentityAdapter.class})
    public StandaloneWorkflowIdentityAdapter standaloneWorkflowIdentityAdapter(
            WorkflowStandaloneProperties properties) {
        return new StandaloneWorkflowIdentityAdapter(properties);
    }

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper standaloneObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    /**
     * workflow-biz 的兼容服务仍保留 ISysBaseAPI 契约（例如抄送用户名称解析）。
     * 独立服务默认按配置提供最小用户目录实现，实际部署可替换为远程用户中心适配器。
     */
    @Bean
    @ConditionalOnMissingBean(ISysBaseAPI.class)
    public ISysBaseAPI standaloneSysBaseApi(WorkflowStandaloneProperties properties) {
        return (ISysBaseAPI) Proxy.newProxyInstance(
                ISysBaseAPI.class.getClassLoader(), new Class<?>[]{ISysBaseAPI.class},
                (proxy, method, args) -> invokeIdentityMethod(properties, method.getName(), args, method.getReturnType()));
    }

    private Object invokeIdentityMethod(WorkflowStandaloneProperties properties, String methodName,
                                        Object[] args, Class<?> returnType) {
        Object argument = args != null && args.length > 0 ? args[0] : null;
        switch (methodName) {
            case "getUserById":
                return userById(properties, argument == null ? null : String.valueOf(argument));
            case "getUserByName":
                return userByUsername(properties, argument == null ? null : String.valueOf(argument));
            case "getRolesByUsername":
            case "getRolesByUserId":
                return roles(properties, String.valueOf(argument), methodName.endsWith("UserId"));
            case "getDepartIdsByUsername":
            case "getDepartIdsByUserId":
                return departments(properties, String.valueOf(argument), methodName.endsWith("UserId"));
            case "getDepartIdsByOrgCode":
                return safeMap(properties.getDepartmentNames()).containsKey(argument)
                        ? String.valueOf(argument) : null;
            case "getDepartNamesByUsername":
                return departmentNames(properties, String.valueOf(argument));
            case "getDepartParentIdsByDepIds":
                return parentDepartments(properties, argument);
            case "getDepartIdsByUserIds":
                return departmentsByUsers(properties, argument);
            case "getDeptHeadByDepId":
                return leader(properties, argument == null ? null : String.valueOf(argument));
            case "queryUserIdsByRoleds":
                return userIdsByKeys(properties.getRoleUsers(), argument);
            case "queryUserIdsByDeptIds":
                return userIdsByKeys(properties.getDepartmentUsers(), argument);
            case "queryUserIdsByPositionIds":
            case "queryUserIdsByDeptPostIds":
                return userIdsByKeys(properties.getPositionUsers(), argument);
            case "queryUsernameByIds":
                return usernamesByIds(properties, argument);
            default:
                return defaultValue(returnType);
        }
    }

    private LoginUser userById(WorkflowStandaloneProperties properties, String userId) {
        if (!knownUser(properties, userId)) {
            return null;
        }
        boolean defaultUser = Objects.equals(userId, properties.getUserId());
        String configuredName = safeMap(properties.getUserNames()).getOrDefault(userId, userId);
        String username = defaultUser ? properties.getUsername() : configuredName;
        String realname = defaultUser ? properties.getDisplayName() : configuredName;
        List<String> departments = departments(properties, userId, true);
        List<String> roles = roles(properties, userId, true);
        return new LoginUser()
                .setId(userId)
                .setUsername(username)
                .setRealname(realname)
                .setOrgId(departments.stream().findFirst().orElse(null))
                .setOrgCode(departments.stream().findFirst().orElse(null))
                .setRoleCode(String.join(",", roles))
                .setDepartIds(String.join(",", departments))
                .setStatus(1)
                .setDelFlag(0);
    }

    private LoginUser userByUsername(WorkflowStandaloneProperties properties, String username) {
        if (Objects.equals(username, properties.getUsername())) {
            return userById(properties, properties.getUserId());
        }
        return safeMap(properties.getUserNames()).entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), username))
                .map(entry -> userById(properties, entry.getKey()))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    private boolean knownUser(WorkflowStandaloneProperties properties, String userId) {
        if (userId == null || userId.isBlank()) return false;
        if (Objects.equals(userId, properties.getUserId())) return true;
        if (safeMap(properties.getUserNames()).containsKey(userId)) return true;
        return allConfiguredUserIds(properties).contains(userId);
    }

    private Set<String> allConfiguredUserIds(WorkflowStandaloneProperties properties) {
        Set<String> ids = new LinkedHashSet<>();
        addValues(ids, properties.getRoleUsers());
        addValues(ids, properties.getDepartmentUsers());
        addValues(ids, properties.getPositionUsers());
        addValues(ids, properties.getUserGroupUsers());
        ids.addAll(safeMap(properties.getDepartmentLeaders()).values());
        addValues(ids, properties.getDepartmentLeaderUsers());
        return ids;
    }

    private List<String> roles(WorkflowStandaloneProperties properties, String value, boolean userId) {
        if (!userId && Objects.equals(value, properties.getUsername())) return safeList(properties.getRoleCodes());
        if (userId && Objects.equals(value, properties.getUserId())) return safeList(properties.getRoleCodes());
        return safeMap(properties.getRoleUsers()).entrySet().stream()
                .filter(entry -> safeList(entry.getValue()).contains(userId ? value : userId))
                .map(Map.Entry::getKey).toList();
    }

    private List<String> departments(WorkflowStandaloneProperties properties, String value, boolean userId) {
        if (!userId && Objects.equals(value, properties.getUsername())) return safeList(properties.getDepartmentIds());
        if (userId && Objects.equals(value, properties.getUserId())) return safeList(properties.getDepartmentIds());
        return safeMap(properties.getDepartmentUsers()).entrySet().stream()
                .filter(entry -> safeList(entry.getValue()).contains(value))
                .map(Map.Entry::getKey).toList();
    }

    private List<String> departmentNames(WorkflowStandaloneProperties properties, String username) {
        return departments(properties, username, false).stream()
                .map(id -> safeMap(properties.getDepartmentNames()).getOrDefault(id, id)).toList();
    }

    private Set<String> parentDepartments(WorkflowStandaloneProperties properties, Object argument) {
        if (!(argument instanceof Set<?> ids)) return Collections.emptySet();
        return ids.stream().map(String::valueOf).map(safeMap(properties.getParentDepartments())::get)
                .filter(Objects::nonNull).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<String, List<String>> departmentsByUsers(WorkflowStandaloneProperties properties, Object argument) {
        if (!(argument instanceof Collection<?> userIds)) return Collections.emptyMap();
        Map<String, List<String>> result = new java.util.LinkedHashMap<>();
        userIds.forEach(id -> result.put(String.valueOf(id), departments(properties, String.valueOf(id), true)));
        return result;
    }

    private List<String> leader(WorkflowStandaloneProperties properties, String departmentId) {
        Set<String> leaders = new LinkedHashSet<>();
        addValues(leaders, safeMap(properties.getDepartmentLeaderUsers()).get(departmentId));
        String leader = safeMap(properties.getDepartmentLeaders()).get(departmentId);
        if (leader != null && !leader.isBlank()) leaders.add(leader.trim());
        return List.copyOf(leaders);
    }

    private List<String> userIdsByKeys(Map<String, List<String>> values, Object argument) {
        if (!(argument instanceof Collection<?> keys)) return Collections.emptyList();
        Set<String> ids = new LinkedHashSet<>();
        keys.forEach(key -> addValues(ids, safeMap(values).get(String.valueOf(key))));
        return List.copyOf(ids);
    }

    private List<String> usernamesByIds(WorkflowStandaloneProperties properties, Object argument) {
        if (!(argument instanceof Collection<?> ids)) return Collections.emptyList();
        return ids.stream().map(String::valueOf)
                .map(id -> safeMap(properties.getUserNames()).getOrDefault(id, id)).toList();
    }

    private void addValues(Set<String> target, Map<String, List<String>> values) {
        safeMap(values).values().forEach(value -> addValues(target, value));
    }

    private void addValues(Set<String> target, Collection<String> values) {
        if (values != null) values.stream().filter(Objects::nonNull).map(String::trim)
                .filter(value -> !value.isBlank()).forEach(target::add);
    }

    private <K, V> Map<K, V> safeMap(Map<K, V> values) {
        return values == null ? Collections.emptyMap() : values;
    }

    private List<String> safeList(List<String> values) {
        return values == null ? Collections.emptyList() : values.stream().filter(Objects::nonNull)
                .map(String::trim).filter(value -> !value.isBlank()).distinct().toList();
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            if (Collection.class.isAssignableFrom(returnType)) return Collections.emptyList();
            if (Map.class.isAssignableFrom(returnType)) return Collections.emptyMap();
            return null;
        }
        if (returnType == boolean.class) return false;
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0F;
        if (returnType == double.class) return 0D;
        if (returnType == char.class) return (char) 0;
        if (returnType.isArray()) return Array.newInstance(returnType.getComponentType(), 0);
        return null;
    }
}
