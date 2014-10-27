package com.xiaomi.smsspam;

public class Options {

//    public static final String FilePath = "/home/qinqiuping/文档/machine_learn/msm_cat/merged.json";
//    public static final String FilePath = "/home/qinqiuping/文档/machine_learn/msm_cat/checked1.json";
//   public static final String FilePath = "/home/qinqiuping/文档/machine_learn/msm_cat/merged_2.json_C_C";

	//public static final String FilePath = "/home/wangsirui/workspace/merged_2.json_C_C";
	
	public static final String FilePath = "/home/dy/IdeaProjects/SmsSpamClassify/data/trainning_data.txt";

    static final int CROSS_COUNT = 10;

    static final int V = 3500 * (3500 - 1);
	static final int COUNT = 8;
    static final int THREAD_COUNT = 5;

    public static final boolean ONLY_DICT_WORD = true;
    
    public static final boolean ONLY_HIGH_IG = true;
    
    public static final boolean COUNT_ALL_HIGH_IG = true;

    public static final int WORD_COUNT_BY_IG = 5000;

    public static final boolean TEST_ALG = true;//true

    public static final boolean TEST_RULES = false;

    public static final boolean FIXED_PRIOR_PROB = true;
    
    public static final double SPAM_RATIO_THRESHOLD = 5d;//3.5
    public static final double SPAM_RATIO_THRESHOLD_LOG = 1.0d;//3.5

    public static final int WORD_COUNT_BY_IG_CLASSIFY = 15;
    
    public static final boolean USE_NEW_PHRASE = true;

    public static final double NEW_PHRASE_IG_THRESH_VALUE = 0.0002f;

    public static final boolean DO_PHRASE_EXPLOER = false;

    public static final boolean DO_DISORGANIZING = false;
    
    public static final String MODEL_FILE_PATH = "/home/dy/IdeaProjects/SmsSpamClassify/data/model";
    
    public static final String PREPROCESS_PACKAGE = "com.xiaomi.smsspam.preprocess";
    
    public static final String ORIGIN_DICT = "/home/wangsirui/workspace/jieba.dict.utf8";
    
    public static final String NEW_DICT = "/home/dy/IdeaProjects/SmsSpamClassify/data/newdict";
}
