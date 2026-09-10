package org.jeecg.modules.workflow.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.entity.WorkflowTenantEntity;

import java.util.Date;

abstract class WorkflowMetadataServiceSupport {
    private final WorkflowIdentityAdapter identityAdapter;

    protected WorkflowMetadataServiceSupport(WorkflowIdentityAdapter identityAdapter) {
        this.identityAdapter = identityAdapter;
    }

    protected long currentTenantId() {
        try {
            return Long.parseLong(identityAdapter.currentTenantId());
        } catch (NumberFormatException exception) {
            throw new JeecgBootException("当前租户编号不是有效数字");
        }
    }

    protected void prepareCreate(WorkflowTenantEntity entity) {
        Date now = new Date();
        String username = identityAdapter.currentUsername();
        entity.setTenantId(currentTenantId());
        entity.setCreator(username);
        entity.setCreateTime(now);
        entity.setUpdater(username);
        entity.setUpdateTime(now);
        entity.setDeleted(0);
    }

    protected void prepareUpdate(WorkflowTenantEntity entity) {
        entity.setUpdater(identityAdapter.currentUsername());
        entity.setUpdateTime(new Date());
    }

    protected void assertChanged(int changed, String message) {
        if (changed != 1) {
            throw new JeecgBootException(message);
        }
    }
}
