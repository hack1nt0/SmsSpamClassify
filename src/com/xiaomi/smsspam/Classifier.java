package com.xiaomi.smsspam;

import com.xiaomi.smsspam.Utils.Corpus;

import java.util.List;

/**
 * Created by dy on 14-10-23.
 */
public abstract class Classifier {
    public abstract void train(List<Corpus> cps);

    public abstract boolean classify(Corpus cps);

    public abstract void saveModel(String filePath);

    public abstract void readModel(String filePath);

}
