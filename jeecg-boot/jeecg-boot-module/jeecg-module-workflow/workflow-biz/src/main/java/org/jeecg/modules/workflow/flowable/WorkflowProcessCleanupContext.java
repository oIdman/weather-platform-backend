package org.jeecg.modules.workflow.flowable;

/**
 * Marks the current thread while a process instance is being purged.
 *
 * <p>Flowable may publish activity-cancelled events after the runtime row has
 * already been removed. In that window a suspended task cannot accept local
 * variables, so listeners must not enrich cancellation metadata.</p>
 */
public final class WorkflowProcessCleanupContext {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private WorkflowProcessCleanupContext() {
    }

    public static void enter() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void exit() {
        int depth = DEPTH.get() - 1;
        if (depth <= 0) {
            DEPTH.remove();
        } else {
            DEPTH.set(depth);
        }
    }

    public static boolean isActive() {
        return DEPTH.get() > 0;
    }
}
