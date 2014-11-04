package com.xiaomi.smsspam.Utils;

import java.io.*;

/**
 * Created by dy on 14-11-3.
 */
public class myWriter {
    ObjectOutputStream out;

    public myWriter(OutputStream out) {
        try {
            this.out = new ObjectOutputStream(new BufferedOutputStream(out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(int val) {
        try {
            out.writeInt(val);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(double val) {
        try {
            out.writeDouble(val);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(long val) {
        try {
            out.writeLong(val);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(boolean val) {
        try {
            out.writeBoolean(val);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(Object val) {
        try {
            out.writeObject(val);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeString(Object val) {
        try {
            out.writeUTF(val.toString());
            out.writeUTF("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
