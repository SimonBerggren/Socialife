package com.berggrentech.socialife;

import java.util.LinkedList;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */
class Buffer<T> {
    private LinkedList<T> buffer = new LinkedList<T>();

    public synchronized void insert(T element) {
        buffer.addLast(element);
        notifyAll();
    }

    public synchronized T pop() throws InterruptedException {
        while(buffer.isEmpty()) {
            wait();
        }
        return buffer.removeFirst();
    }
}
