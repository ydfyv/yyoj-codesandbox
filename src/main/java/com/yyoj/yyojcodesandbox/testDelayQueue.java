package com.yyoj.yyojcodesandbox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * 延时执行
 * 例如：订单超过30分钟未支付，即取消订单
 * 延时30分钟执行，超过30分钟后，将该订单取出，确认状态，若还是未支付则删除
 */

public class testDelayQueue {
    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DelayQueue<Order> orders = new DelayQueue<>();
        System.out.println(df.format(new Date()) + "订单A加入队列");
        //生成订单A并加入队列
        Order order_A = new Order("ALiangX","testA_" +random.nextInt(100),10,TimeUnit.SECONDS);
        orders.offer(order_A);
        Thread.sleep(5000);
        //等待5s生成订单B并加入队列
        System.out.println(df.format(new Date()) + "订单B加入队列");
        Order order_B = new Order("Jack Ma","testB_" +random.nextInt(100),10,TimeUnit.SECONDS);
        orders.offer(order_B);

        Order peek = orders.peek();
        System.out.println("订单" + peek.toString() + "在" + df.format(new Date()) + "被取出！！");

        Thread.sleep(5000);
        //再等待5s生成订单C并加入队列
        System.out.println(df.format(new Date()) + "订单C加入队列");
        Order order_C = new Order("Pony","testC_" +random.nextInt(100),10,TimeUnit.SECONDS);
        orders.offer(order_C);
        while(!orders.isEmpty()){
            //若没有超时的元素，take()会阻塞该线程
            Order order = orders.take();
            System.out.println("订单因为未支付：" +order.toString() + " 在 " + df.format(new Date()) + "被取出！！" );
        }
    }
}
