package com.xiaomi.smsspam.Utils;

import java.io.*;
import java.util.Scanner;

/**
 * Created by dy on 14-11-3.
 */
public class myReader {
    ObjectInputStream in;

    public myReader(InputStream in) {
        try {
            this.in = new ObjectInputStream(new BufferedInputStream(in));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Integer getInt() {
        try {
            return in.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Double getDouble() {
        try {
            return in.readDouble();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Long getLong() {
        try {
            return in.readLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean getBoolean() {
        try {
            return in.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getObject() {
        try {
            return in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object getString() {
        try {
            return in.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
