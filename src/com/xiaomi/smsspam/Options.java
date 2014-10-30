package com.xiaomi.smsspam;

public class Options {
    public static int SPAM = 0;
    public static int NORMAL = 1;

    public static final String PATH = "/home/qinqiuping/nt/files/spamsms/version2/";
    public static final String FilePath = "/home/wangsirui/workspace/merged_2_C_C_C .json";

    public static final int CROSS_COUNT = 10;

    public static final boolean PRINT_CUR_RULES = true;

    static final int COUNT = 8;
    static final int THREAD_COUNT = 8;

    public static final boolean ONLY_DICT_WORD = false;
    
    public static final boolean ONLY_HIGH_IG = true;
    
    public static final boolean COUNT_ALL_HIGH_IG = true;

    public static final int WORD_COUNT_BY_IG = 5000;

    public static final boolean TEST_ALG = false;//true

    public static final boolean TEST_RULES = false;

    public static final boolean FIXED_PRIOR_PROB = true;
    
    public static final double SPAM_RATIO_THRESHOLD = 1;//3.5
    public static final double SPAM_RATIO_THRESHOLD_LOG = 1.0d;//3.5

    public static final int WORD_COUNT_BY_IG_CLASSIFY = 15;
    
    public static final boolean USE_NEW_PHRASE = true;

    public static final double NEW_PHRASE_IG_THRESH_VALUE = 0.0002f;

    public static final boolean DO_PHRASE_EXPLOER = false;

    public static final boolean DO_DISORGANIZING = false;
    
    public static final String MODEL_FILE_PATH = "/home/wangsirui/workspace/model";
    

    public static final String ORIGIN_DICT = "/home/wangsirui/workspace/jieba.dict.utf8";
    
    public static final String NEW_DICT = "/home/wangsirui/workspace/newdict";
}
