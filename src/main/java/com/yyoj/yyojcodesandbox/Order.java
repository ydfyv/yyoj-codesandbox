package com.yyoj.yyojcodesandbox;

import lombok.Data;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Data
public class Order implements Delayed {
    private String user;
    private String order_id;
    private long order_time;

    public Order(String user, String order_id, long order_time, TimeUnit timeUnit) {
        this.user = user;
        this.order_id = order_id;
        this.order_time = System.currentTimeMillis() + (order_time > 0 ? timeUnit.toMillis(order_time) : 0);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return order_time - System.currentTimeMillis();
    }

    @Override
    public int compareTo(Delayed o) {
        Order order = (Order) o;
        long to = this.order_time - order.getOrder_time();
        return to <= 0 ? -1 : 1;
    }

    @Override
    public String toString() {
        return "Order{" +
                "user='" + user + '\'' +
                ", order_id='" + order_id + '\'' +
                ", order_time=" + order_time +
                '}';
    }
}


