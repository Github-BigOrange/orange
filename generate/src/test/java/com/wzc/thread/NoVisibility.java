package com.wzc.thread;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 在没有同步的情况下共享变量(不合适的做法)
 * @author 王志成
 * @date 2022年10月14日 14:24
 */
//@SpringBootTest
public class NoVisibility {
    private static boolean ready;
    private static int number;

    private static class ReaderThread extends Thread{
        public void run(){
            while (!ready){
                Thread.yield();
            }
            System.out.println(number);

        }
    }
//    @Test
    public static void main(String[] args) {
        System.out.println("开始执行");
        for (int i = 0; i < 1000; i++) {
            f();
        }


    }


    public static void  f(){
        new ReaderThread().start();
        number = 42;
        ready = true;
    }
}
