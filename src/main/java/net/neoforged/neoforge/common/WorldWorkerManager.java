package net.neoforged.neoforge.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages background workers that perform tick-based chunked tasks.
 * Workers are processed during server ticks, dividing available time among them.
 */
public class WorldWorkerManager {
    private static List<IWorker> workers = new ArrayList<>();
    private static long startTime = -1;
    private static int index = 0;

    public static void tick(boolean start) {
        if (start) {
            startTime = System.currentTimeMillis();
            return;
        }

        index = 0;
        IWorker task = getNext();
        if (task == null) return;

        long time = 50 - (System.currentTimeMillis() - startTime);
        if (time < 10) time = 10;
        time += System.currentTimeMillis();

        while (System.currentTimeMillis() < time && task != null) {
            boolean again = task.doWork();
            if (!task.hasWork()) {
                remove(task);
                task = getNext();
            } else if (!again) {
                task = getNext();
            }
        }
    }

    public static synchronized void addWorker(IWorker worker) {
        workers.add(worker);
    }

    private static synchronized IWorker getNext() {
        return workers.size() > index ? workers.get(index++) : null;
    }

    private static synchronized void remove(IWorker worker) {
        workers.remove(worker);
        index--;
    }

    public static synchronized void clear() {
        workers.clear();
    }

    public interface IWorker {
        boolean hasWork();

        /**
         * Perform a task. Return true to be called again this tick if time remains.
         * Return false to skip until the next tick.
         */
        boolean doWork();
    }
}
