package com.berggrentech.socialife;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */

class ThreadPool {
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

    void execute(Runnable runnable) {
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

