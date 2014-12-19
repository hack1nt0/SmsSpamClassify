package com.xiaomi.smsspam;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dy on 14-12-5.
 */
public class T {
    interface A {

    }

    class B implements A {

    }

    public static void get(Map<Integer, ArrayList<ArrayList<Object>>> map) {
    }

    public static void change(Integer i) {
        i += 1;
    }

    public static void main(String[] args) {
        Map<Integer, ArrayList<B>> map = new HashMap<>();
        Map<Integer, ArrayList<ArrayList<Object>>> map1 = new HashMap<>();
        Integer i = 1;
        change(i);
        System.out.println(i);
        get(map1);
    }
}
