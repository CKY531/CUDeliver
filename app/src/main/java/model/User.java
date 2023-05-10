package model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class User {

    private String email;
    private String phone;

    private List<Order> myOrders;
    private List<Order> myJobs;

    public User(){

    }

    public User(String email,String phone){
        this.email = email;
        this.phone = phone;
        myJobs = new LinkedList<Order>();
        myOrders = new LinkedList<Order>();
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

    public List<Order> getMyJobs() {
        return myJobs;
    }

    public void addNewJob(Order order) {
        myJobs.add(order);
    }

    public List<Order> getMyOrders() {
        return myOrders;
    }

    public void addNewOrder(Order order) {
        myOrders.add(order);
    }
}
