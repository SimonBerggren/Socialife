package com.berggrentech.socialife;

import java.util.LinkedList;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */

class TaskManager {

    private class Buffer<T> {
        private LinkedList<T> buffer = new LinkedList<T>();

        synchronized void insert(T element) {
            buffer.addLast(element);
            notifyAll();
        }

        synchronized T pop() throws InterruptedException {
            while(buffer.isEmpty()) {
                wait();
            }
            return buffer.removeFirst();
        }
    }

    private Buffer<Runnable> tasks = new Buffer<>();
    private WorkerThread worker;

    void start() {
        if(worker==null) {
            worker = new WorkerThread();
            worker.start();
        }
    }

    void stop() {
        if(worker!=null) {
            worker.interrupt();
            worker=null;
        }
    }

    void addTask(Runnable runnable) {
        tasks.insert(runnable);
    }

    private class WorkerThread extends Thread {
        public void run() {
            Runnable runnable;
            while(worker!=null) {
                try {
                    runnable = tasks.pop();
                    runnable.run();
                } catch (InterruptedException e) {
                    worker=null;
                }
            }
        }
    }
}

