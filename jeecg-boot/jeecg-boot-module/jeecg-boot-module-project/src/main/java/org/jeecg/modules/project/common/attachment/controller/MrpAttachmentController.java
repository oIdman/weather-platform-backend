package org.jeecg.modules.project.common.attachment.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.project.common.attachment.entity.MrpAttachment;
import org.jeecg.modules.project.common.attachment.service.IMrpAttachmentService;
import org.jeecg.modules.project.common.enums.AttachmentBizTypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: 公共附件（上传 / 下载 / 删除 / 绑定业务对象）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Tag(name = "公共附件")
@RestController
@RequestMapping("/project/attachment")
@Slf4j
public class MrpAttachmentController extends JeecgController<MrpAttachment, IMrpAttachmentService> {

    private static final List<String> ALLOWED_EXT = Arrays.asList(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf",
            "png", "jpg", "jpeg", "gif", "bmp",
            "zip", "rar", "7z", "tar", "gz",
            "txt", "md", "csv", "xml", "bpmn", "json");

    @Value("${jeecg.path.upload}")
    private String uploadPath;

    @AutoLog(value = "附件上传")
    @Operation(summary = "附件上传", description = "上传文件并登记到 mrp_attachment；bizId 可留空，后续通过 bind 接口绑定业务对象")
    @PostMapping(value = "/upload")
    public Result<MrpAttachment> upload(@RequestParam("file") MultipartFile file,
                                        @RequestParam("bizType") String bizType,
                                        @RequestParam(value = "bizId", required = false) String bizId) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空！");
        }
        if (!AttachmentBizTypeEnum.contains(bizType)) {
            return Result.error("非法业务类型：" + bizType);
        }
        String originalName = sanitizeFileName(file.getOriginalFilename());
        if (oConvertUtils.isEmpty(originalName)) {
            return Result.error("文件名不合法！");
        }
        String ext = getExtension(originalName);
        if (!ALLOWED_EXT.contains(ext.toLowerCase())) {
            return Result.error("不支持的文件类型：" + ext);
        }
        try {
            // 存储目录：{uploadPath}/mrp/{bizType}/{yyyyMMdd}/
            String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String bizDir = "mrp/" + bizType + "/" + dateDir;
            File dir = new File(uploadPath, bizDir);
            if (!dir.exists() && !dir.mkdirs()) {
                return Result.error("创建上传目录失败！");
            }
            String storeName = originalName.substring(0, originalName.lastIndexOf("."))
                    + "_" + System.currentTimeMillis() + "." + ext;
            File target = new File(dir, storeName);
            FileCopyUtils.copy(file.getBytes(), target);

            MrpAttachment attachment = new MrpAttachment();
            attachment.setBizType(bizType);
            attachment.setBizId(bizId);
            attachment.setFileName(originalName);
            attachment.setFileUrl(bizDir + "/" + storeName);
            attachment.setFileType(ext.toLowerCase());
            attachment.setFileSize(file.getSize());
            attachment.setUploadBy(getLoginUsername());
            service.save(attachment);
            return Result.OK("上传成功！", attachment);
        } catch (IOException e) {
            log.error("附件上传失败", e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "附件分页列表查询", description = "按 bizType / bizId 等条件分页查询")
    @GetMapping(value = "/list")
    public Result<IPage<MrpAttachment>> queryPageList(MrpAttachment mrpAttachment,
                                                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                      HttpServletRequest req) {
        QueryWrapper<MrpAttachment> queryWrapper = QueryGenerator.initQueryWrapper(mrpAttachment, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<MrpAttachment> page = new Page<>(pageNo, pageSize);
        IPage<MrpAttachment> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "附件通过id查询", description = "附件通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpAttachment> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpAttachment attachment = service.getById(id);
        if (attachment == null) {
            return Result.error("未找到对应附件");
        }
        return Result.ok(attachment);
    }

    @Operation(summary = "附件下载", description = "按附件ID下载文件")
    @GetMapping(value = "/download")
    public void download(@RequestParam(name = "id", required = true) String id, HttpServletResponse response) throws IOException {
        MrpAttachment attachment = service.getById(id);
        if (attachment == null || oConvertUtils.isEmpty(attachment.getFileUrl())) {
            response.setStatus(404);
            return;
        }
        String relativePath = attachment.getFileUrl();
        if (relativePath.contains("../") || relativePath.contains("..\\")) {
            response.setStatus(400);
            return;
        }
        File file = new File(uploadPath + File.separator + relativePath.replace("/", File.separator));
        if (!file.exists()) {
            response.setStatus(404);
            return;
        }
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName="
                + new String(file.getName().getBytes("UTF-8"), "iso-8859-1"));
        response.setContentLengthLong(file.length());
        try (InputStream inputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            response.flushBuffer();
        }
    }

    @AutoLog(value = "附件删除")
    @Operation(summary = "附件删除", description = "删除附件记录并清理物理文件")
    @DeleteMapping(value = "/delete")
    public Result<MrpAttachment> delete(@RequestParam(name = "id", required = true) String id) {
        MrpAttachment attachment = service.getById(id);
        if (attachment == null) {
            return Result.error("未找到对应附件");
        }
        deletePhysicalFile(attachment.getFileUrl());
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "附件绑定业务对象", description = "上传后补充/修改 bizType、bizId")
    @PostMapping(value = "/bind")
    public Result<MrpAttachment> bind(@RequestBody MrpAttachment mrpAttachment) {
        if (oConvertUtils.isEmpty(mrpAttachment.getId())) {
            return Result.error("附件ID不能为空！");
        }
        MrpAttachment byId = service.getById(mrpAttachment.getId());
        if (byId == null) {
            return Result.error("未找到对应附件");
        }
        if (oConvertUtils.isNotEmpty(mrpAttachment.getBizType()) && !AttachmentBizTypeEnum.contains(mrpAttachment.getBizType())) {
            return Result.error("非法业务类型：" + mrpAttachment.getBizType());
        }
        byId.setBizType(mrpAttachment.getBizType());
        byId.setBizId(mrpAttachment.getBizId());
        service.updateById(byId);
        return Result.OK("绑定成功！", byId);
    }

    private void deletePhysicalFile(String relativePath) {
        if (oConvertUtils.isEmpty(relativePath) || relativePath.contains("../") || relativePath.contains("..\\")) {
            return;
        }
        try {
            File file = new File(uploadPath + File.separator + relativePath.replace("/", File.separator));
            if (file.exists() && !file.delete()) {
                log.warn("物理文件删除失败：{}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("物理文件删除异常", e);
        }
    }

    private String sanitizeFileName(String name) {
        if (oConvertUtils.isEmpty(name)) {
            return null;
        }
        String fileName = name.replace("\\", "/");
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        return fileName;
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf(".");
        return dot >= 0 ? fileName.substring(dot + 1) : "";
    }

    private String getLoginUsername() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof LoginUser) {
                return ((LoginUser) principal).getUsername();
            }
        } catch (Exception e) {
            log.debug("获取登录用户失败，uploadBy 置空", e);
        }
        return null;
    }
}
