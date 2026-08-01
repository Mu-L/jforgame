package jforgame.commons.persist;

import java.util.Arrays;

/**
 * Persistence in queue group form
 * Combine several queue containers into a queue group, perform modulo operation based on entity id, similar to database table partitioning strategy
 */
public class DelayContainerGroup extends BasePersistContainer {

    /**
     * Container group
     */
    private DelayContainer[] group;

    public DelayContainerGroup(String name, SavingStrategy savingStrategy, int workers, int delaySeconds) {
        group = new DelayContainer[workers];
        for (int i = 0; i < workers; i++) {
            DelayContainer work = new DelayContainer(name, delaySeconds, savingStrategy);
            group[i] = work;
        }
        this.name = name + "-group";
    }

    @Override
    public void receive(Entity<?> entity) {
        int index = Math.abs(entity.getId().hashCode()) % group.length;
        group[index].receive(entity);
    }

    @Override
    public int size() {
        int size = 0;
        for (DelayContainer queueContainer : group) {
            size += queueContainer.size();
        }
        return size;
    }

    @Override
    protected void saveAllBeforeShutdown() {
        for (DelayContainer queueContainer : group) {
            queueContainer.saveAllBeforeShutdown();
        }
    }

    @Override
    public String toString() {
        return String.format(
                "DelayContainerGroup{groupName='%s', workerCount=%d, children=%s}",
                this.name,
                group.length,
                Arrays.toString(group)
        );
    }
}