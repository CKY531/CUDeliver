package model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class User {

    private String email;
    private String phone;

//    private List<Order> myOrders;
    private HashMap<String,Order> myJobs;

    private HashMap<String,Order> myOrders;

    public User(){

    }

    public User(String email,String phone){
        this.email = email;
        this.phone = phone;
        myJobs = new HashMap<String,Order>();
        myOrders = new HashMap<String, Order>();

    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public HashMap<String,Order> getMyJobs() {
        return myJobs;
    }

    public List<Order> getMyJobList(){
        LinkedList<Order> l = new LinkedList<>(myJobs.values());
        return l;
    }

    public void addNewJob(String orderId,Order order) {
        myJobs.put(orderId,order);
    }

    public HashMap<String, Order> getMyOrders() {
        return myOrders;
    }

    public List<Order> getMyOrderList(){
        LinkedList<Order> l = new LinkedList<>(myOrders.values());
        return l;
    }

    public void addNewOrder(String orderId,Order order) {
        myOrders.put(orderId,order);
//        myOrders.add(order);
    }
}
