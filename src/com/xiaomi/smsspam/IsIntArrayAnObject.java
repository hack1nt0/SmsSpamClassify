package com.xiaomi.smsspam;

import java.util.Arrays;

/**
 * Created by dy on 14-10-25.
 */
public class IsIntArrayAnObject {
    public static void main(String[] args) {
        int[] arr = {1, 2};
        changeArr(arr);
        System.out.println(arr[0]);
    }

    private static void changeArr(int[] arr) {
        arr[0] = 123231;
    }
}
