package com.zqh.zookeeper.dist.mutex;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BooleanMutexTest{

	@Test
    public void test_init_true() {
        BooleanMutex mutex = new BooleanMutex(true);
        try {
            mutex.get(); //不会被阻塞
        } catch (InterruptedException e) {
        }
    }

    @Test
    public void test_init_false() {
        final BooleanMutex mutex = new BooleanMutex(false);
        try {
            final CountDownLatch count = new CountDownLatch(1);
            ExecutorService executor = Executors.newCachedThreadPool();

            executor.submit(new Callable() {

                public Object call() throws Exception {
                    Thread.sleep(1000);
                    mutex.set(true);
                    count.countDown();
                    return null;
                }
            });

            mutex.get(); //会被阻塞，等异步线程释放锁对象
            count.await();
            executor.shutdown();
        } catch (InterruptedException e) {
        }
    }

    @Test
    public void test_concurrent_true() {
        try {
            final BooleanMutex mutex = new BooleanMutex(true);
            final CountDownLatch count = new CountDownLatch(10);
            ExecutorService executor = Executors.newCachedThreadPool();

            for (int i = 0; i < 10; i++) {
                executor.submit(new Callable() {

                    public Object call() throws Exception {
                        mutex.get();
                        count.countDown();
                        return null;
                    }
                });
            }
            count.await();
            executor.shutdown();
        } catch (InterruptedException e) {
        }
    }

    @Test
    public void test_concurrent_false() {
        try {
            final BooleanMutex mutex = new BooleanMutex(false);//初始为false
            final CountDownLatch count = new CountDownLatch(10);
            ExecutorService executor = Executors.newCachedThreadPool();

            for (int i = 0; i < 10; i++) {
                executor.submit(new Callable() {

                    public Object call() throws Exception {
                        mutex.get();//被阻塞
                        count.countDown();
                        return null;
                    }
                });
            }
            Thread.sleep(1000);
            mutex.set(true);
            count.await();
            executor.shutdown();
        } catch (InterruptedException e) {
        }
    }
	
}
