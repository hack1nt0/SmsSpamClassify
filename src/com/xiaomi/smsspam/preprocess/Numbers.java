package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Only ASCII and UTF digits are considered as normal digits
 * Assume there is a main digit type, either ASCII or UTF
 * The first digit is the main type
 * The last digit is main type too
 * @author qinqiuping
 *
 */
public class Numbers extends RulePrevious {
	
	private static final String NUMBERS_ASCII = "0123456789";
    private static final String NUMBERS_UTF = "０１２３４５６７８９";
    private static final String NUMBERS_UTF1 = "①②③④⑤⑥⑦⑧⑨";
    private static final String NUMBERS_UTF2 = "⒈⒉⒊⒋⒌⒍⒎⒏⒐";
    private static final String NUMBERS_UTF3 = "⓵⓶⓷⓸⓹⓺⓻⓼⓽";
    private static final String NUMBERS_CN = "零一二三四五六七八九";
    private static final String NUMBERS_TW = "零壹贰叁肆伍陆柒捌玖";

    private static final char SPECIAL_DIGIT = '一';  // Do not treat it as digit, as a connector

    private static final String[] FULL_NUMBERS =    {NUMBERS_ASCII, NUMBERS_UTF, NUMBERS_CN, NUMBERS_TW};
    private static final String[] NO_ZERO_NUMBERS = {NUMBERS_UTF1, NUMBERS_UTF2, NUMBERS_UTF3};
    
    private static final String NUMBERS_CONFUSION =     "I|loOBbgqzZ";
    private static final String NUMBERS_CONFUSION_MAP = "11100869922";
    private static final String NUMBERS_CONFUSION_UTF =     "｜ｏｑｌｂＯＩＢＺｚ";
    private static final String NUMBERS_CONFUSION_UTF_Map = "1091601822";

    private static final String[][] CONFUSION_MAP = {{NUMBERS_CONFUSION, NUMBERS_CONFUSION_MAP},
                                                     {NUMBERS_CONFUSION_UTF, NUMBERS_CONFUSION_UTF_Map}};
    private static final int CONFUSION_INDEX = 0;
    private static final int ASCII_INDEX = 1;

    // "一" is always considered as connector, not a CN digit
    private static final String NUMBERS_CONNECTOR = "-—一~ 　";
    private static final char POINT = '.';
    private static final String COLONS = ":：";
    private static final String RANGE_SYMBOLS = "-—一~";

    // ASCII:0, UTF:1
    protected static final String[] NUMBERS = {
                                                NUMBERS_ASCII,
                                                NUMBERS_UTF,
                                                NUMBERS_UTF1,
                                                NUMBERS_UTF2,
                                                NUMBERS_UTF3,
                                                NUMBERS_CN,
                                                NUMBERS_TW};

    private static final int TAB = 9;
    private static final int NL = 10;
    private static final int CR = 13;

    
   //动态的Names和TypeNames，可根据模型文件生成
    private List<String> dNames = new ArrayList<String>();
    private List<String> dTypeNames = new ArrayList<String>();
   
    
    protected ArrayList<Number> mNumbers = new ArrayList<Number>();
    protected int[] mCounts;// = new int[subClassCount()];

    protected static final int[] BANK_CARD_COUNT = {15, 16, 18, 19};
    protected static final String BANK_CARD_FIRST = "456";

    private static final int BANK_CARD = 0;
    private static final int PHONE = 1;
    private static final int PHONE_AREA = 2;
    private static final int PHONE_MOBILE = 3;
    private static final int PHONE_400 = 4;
    private static final int RANGE = 5;
    private static final int TIME = 6;
    private static final int LONG = 7;
    private static final int OTHER = 8;
    private static final int COUNT = 9;

    private static final String[] Names = {
                                            "BankCard",
                                            "Phone",
                                            "PhoneArea",
                                            "PhoneMobile",
                                            "Phone400",
                                            "Range",
                                            "Time",
                                            "Long",
                                            "Other"
    };


    private static final int ASCII = 0;
    private static final int UTF = 1;
    private static final int PURE = 0;
    private static final int CONFUSION = 1;
    // ASCII:0, UTF:1
    // Pure:0, Confusion:1
    private static final String[] TypeNames = {
                                            "PureASCII",
                                            "PureUTF",
                                            "ConfASCII",
                                            "ConfUTF"
    };
       
    public Numbers()
    {
    	for(int i = 0; i < Names.length; i++)
    	{
    		dNames.add(Names[i]);
    	}
    	for(int i = 0; i < TypeNames.length; i++)
    	{
    		dTypeNames.add(TypeNames[i]);
    	}
    	mCounts = new int[subClassCount()];
    }
    

    public static interface SubClass{
        public boolean fit(String number);
    }

    class Number {
		String mNumber;
        boolean mClassified;
        Number(String n){
            mNumber = n;
        }
    }
    @Override
    public int subClassCount(){
        //return mSubClass.length;
        //return Names.length * TypeNames.length;
    	return dNames.size()*dTypeNames.size();
    }

    @Override
    public String getClassName(int i){
//        int subClass = i / TypeNames.length;
//        int type = i % TypeNames.length;
//        return padding("Number" + "_" + Names[subClass] + "_" + TypeNames[type]);
    	int subClass = i / dTypeNames.size();
        int type = i % dTypeNames.size();
        return padding("Number" + "_" + dNames.get(subClass) + "_" + dTypeNames.get(type));
    }

    @Override
    public void train(List<Corpus> cpss) {

    }

    public void reset(){
        mNumbers.clear();
        for(int i = 0; i < mCounts.length; ++i){
            mCounts[i] = 0;
        }
    }

    @Override
    public boolean fit(Corpus cps, int startIndex) {
        boolean flag = false;
        for(int i = 0; i < mCounts.length; ++i){
            if(mCounts[i] > 0){
                flag = true;
            }
            cps.getRulesPreHits()[startIndex + i] = mCounts[i];
        }
        return flag;
    }

    private static final int MIN_NUMBER_COUNT = 2;
    private static final int RANGE_SECTIONS_COUNT = 2;

    private boolean dispose(String n, int type, boolean hasConnector, char connector, boolean hasMark, char mark){
        if(n.length() <= MIN_NUMBER_COUNT){
            return false;
        }
        int pure = isPureType(n) ? PURE : CONFUSION;
//if(PURE != pure){
//    System.out.println("not pure:" + n);
//}
        int classId = -1;
        ArrayList<String> sections = new ArrayList<String>();

        // Cut the numbers by connector
        if(hasConnector){
            boolean flag = false;
            int lastPos = 0;
            for(int i = 0; i < n.length(); ++i){
                if(n.charAt(i) == connector){
                    if(!flag){
                        // i should bigger than lastPos, according to the rules
                        sections.add(n.substring(lastPos, i));
                        flag = true;
                    }
                    lastPos = i + 1;
                }else{
                    flag = false;
                }
            }
            sections.add(n.substring(lastPos));

            for(int i = 0; i < sections.size(); ++i){
                sections.set(i, convertToPureASCII(sections.get(i)));
            }

            // In this section, type RANGE is over
            if(RANGE_SYMBOLS.indexOf(connector) != -1 && sections.size() == RANGE_SECTIONS_COUNT){
                if(sections.get(0).charAt(0) == NUMBERS_ASCII.charAt(0) && // area code start with 0
                        !hasMark && //n.indexOf(POINT) == -1 &&                   // no marks
                        (sections.get(0).length() == 3 || sections.get(0).length() == 4) &&  // length of area code == 3/4
                        (sections.get(1).length() == 7 || sections.get(1).length() == 8)){   // length of code == 7/8
                    //classId = PHONE_AREA;
                	classId = dNames.indexOf("PhoneArea");
//                    System.out.println("PHONE_AREA:" + n);
                }else {
                    String s0 = sections.get(0);
                    String s1 = sections.get(1);
                    if(hasMark && mark != POINT){
                        s0 = s0.replace(mark, POINT);
                        s1 = s1.replace(mark, POINT);
                    }
                    if(validFloat(s0) && validFloat(s1) &&
                            Double.valueOf(s0) < Double.valueOf(s1)){
                        // 400 phone exception
                        if(sections.get(0).equals("400") && s1.length() == 7 && !hasMark){
                            //classId = PHONE_400;
                        	classId = dNames.indexOf("Phone400");
//                            System.out.println("PHONE_400:" + n);
                        }else{
                            //classId = RANGE;
                        	classId = dNames.indexOf("Range");
//                            System.out.println("RANGE:" + n);
                        }
                    }
                }
            }
        }else{
            sections.add(convertToPureASCII(n));
        }

        if(classId < 0){
            StringBuffer sb = new StringBuffer();
            for(String s : sections){
                sb.append(s.replaceAll("\\.", ""));
            }
            String pureN = sb.toString();
    //        System.out.println("pureN:" + pureN);
    
            int count = pureN.length();
            if(count > 19){
                //classId = LONG;
            	classId = dNames.indexOf("Long");
//                System.out.println("LONG:" + n);
            }else if(count >= 15){ // BankCard
                if(BANK_CARD_FIRST.indexOf(pureN.charAt(0)) != -1 && !(hasMark && hasConnector)){
                    //classId = BANK_CARD;
                	classId = dNames.indexOf("BankCard");
//                    System.out.println("BANK_CARD:" + n);
                }else{
                    //classId = OTHER;
                	classId = dNames.indexOf("Other");
//                    System.out.println("BANK_CARD_OTHER:" + n);
                }
            }else if(count > 12){
                //classId = LONG;
            	classId = dNames.indexOf("Long");
//                System.out.println("LONG:" + n);
            }else if(count >= 11){ // phone with area code, or mobile phone
                if(pureN.charAt(0) == NUMBERS_ASCII.charAt(0)){
                    //classId = PHONE_AREA;
                	classId = dNames.indexOf("PhoneArea");
                }
                if(count == 11 && pureN.charAt(0) == NUMBERS_ASCII.charAt(1)){
                   // classId = PHONE_MOBILE;
                	classId = dNames.indexOf("PhoneMobile");
//                    System.out.println("PHONE_MOBILE:" + n);
                }
            }else if(count == 10){ // 400 phone
                if("400".equals(pureN.substring(0, 3))){
                    //classId = PHONE_400;
                	classId = dNames.indexOf("Phone400");
                }
            }else if(sections.size() == 1 && hasMark && COLONS.indexOf(mark) != -1){
                //classId = TIME;
            	classId = dNames.indexOf("Time");
//                System.out.println("TIME:" + n);
            }else if(!hasMark && !hasConnector && pureN.charAt(0) > '1' && (count == 7 || count == 8)){
                //classId = PHONE;
            	classId = dNames.indexOf("Phone");
//                System.out.println("PHONE:" + n);
            }else{
                //classId = OTHER;
            	classId = dNames.indexOf("Other");
            }
        }

        if(classId >= 0){
            int typeId = (pure * 2) + type;
            //classId *= TypeNames.length;
            classId *= dTypeNames.size();
            classId += typeId;
            mCounts[classId]++;
            return true;
        }else{
//            System.out.println("DROP NUMBER:" + n);
        }
        return false;

    }

    private static boolean validFloat(String f){
//        if(f.length() > 1 && f.charAt(0) == '0' && f.charAt(1) != POINT){
//            return false;
//        }
        return f.indexOf(POINT) == f.lastIndexOf(POINT);
    }

    protected List<String> process(String str) {
        if(null == str){
            return null;
        }
        ArrayList<String> ret = new ArrayList<String>();
        int startPos = -1, endPos = -1, lastPos = 0;
        char connector = '0';
        boolean hasConnector = false;
        int firstConnectorPos = -1;

        int numberType = ASCII;
        
        boolean hasMark = false;  // POINT(.)  COLON(:：)
        char mark = POINT;
//System.out.println("Body:" + str);

        for(int i = 0; i < str.length(); ++i){
            char c = str.charAt(i);
            if(looksLikeNumber(c) && c != SPECIAL_DIGIT){
                if(startPos < 0){
                    //if(isRegularNumber(c)){
                    if(looksLikeArabic(c)){
                        startPos = i;
                        numberType = regularType(c);
                    }else{
                        continue;
                    }
                }
                //if(indexOfNumber(c) == numberType){
                if(looksLikeArabic(c)){
                    endPos = i;
                }
            }else if(startPos >= 0){
                if(isConnector(c) && !hasConnector){
                    connector = c;
                    hasConnector = true;
                    firstConnectorPos = i;
                }
                else if(isMark(c) && !hasMark){
                    mark = c;
                    hasMark = true;
                }
                else if(c != connector && c != mark){
                    if(startPos > lastPos){
                        String subSeg = str.substring(lastPos, startPos);
//System.out.println("C:" + subSeg);
                        ret.add(subSeg);
                    }

                    String nb = str.substring(startPos, endPos + 1);
//System.out.println("N:" + nb);
                    if(hasConnector){
                        hasConnector = firstConnectorPos < endPos;
                    }
                    if(dispose(nb, numberType, hasConnector, connector, hasMark, mark)){
                        mNumbers.add(new Number(nb));
                    }
                    lastPos = endPos + 1;

                    startPos = -1;
                    hasConnector = false;
                    numberType = ASCII;
                    firstConnectorPos = -1;
                    hasMark = false;
                }
            }
        }
        if(startPos >= 0){
            if(startPos > lastPos){
                ret.add(str.substring(lastPos, startPos));
//System.out.println("C:" + ret.get(ret.size() - 1));
            }
            if(endPos + 1 < str.length()){
                ret.add(str.substring(endPos + 1));
//System.out.println("CC:" + ret.get(ret.size() - 1));
            }
            String nb = str.substring(startPos, endPos + 1);
//System.out.println("N:" + nb);
            if(dispose(nb, numberType, hasConnector, connector, hasMark, mark)){
                mNumbers.add(new Number(nb));
            }
        }else{
            ret.add(str.substring(lastPos, str.length()));
//System.out.println("C:" + ret.get(ret.size() - 1));
        }
//        mProcessed = true;
        return ret;
    }

    protected static int indexOfNumber(char c){
        for(int i = 0; i < NUMBERS.length; ++i){
            String s = NUMBERS[i];
            if(s.indexOf(c) != -1){
                return i;
            }
        }
        return -1;
    }

    protected static String convertToPureASCII(String str){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < str.length(); ++i){
            char c = str.charAt(i);
            if(!isConnector(c)){
                sb.append(convertToPureASCII(c));
            }
        }
        return sb.toString();
    }

    protected static char convertToPureASCII(char c){
        int index = -1;
        for(String N : FULL_NUMBERS){
            index = N.indexOf(c);
            if(index != -1){
                return NUMBERS_ASCII.charAt(index);
            }
        }

        for(String N : NO_ZERO_NUMBERS){
            index = N.indexOf(c);
            if(index != -1){
                return NUMBERS_ASCII.charAt(index + 1);
            }
        }
        for(int i = 0; i < CONFUSION_MAP.length; ++i){
            index = CONFUSION_MAP[i][CONFUSION_INDEX].indexOf(c);
            if(index != -1){
                return CONFUSION_MAP[i][ASCII_INDEX].charAt(index);
            }
        }
        return c;
    }

    protected static boolean isConfusionNumber(char c){
        return (NUMBERS_CONFUSION.indexOf(c) != -1) ||
               (NUMBERS_CONFUSION_UTF.indexOf(c) != -1);
    }

    protected static boolean looksLikeNumber(char c){
        return (indexOfNumber(c) >= 0) || isConfusionNumber(c);
    }

    protected static boolean isRegularNumber(char c){
        return NUMBERS_ASCII.indexOf(c) != -1 || NUMBERS_UTF.indexOf(c) != -1;
    }

    protected static int regularType(char c){
        int type = ASCII;
        if(NUMBERS_UTF.indexOf(c) != -1){
            type = UTF;
        }
        return type;
    }

    protected static boolean isConnector(char c){
        return (NUMBERS_CONNECTOR.indexOf(c) != -1)
            || ((int)c == NL) || ((int)c == TAB) || ((int)c == CR);
    }

    private static boolean isMark(char c){
        return COLONS.indexOf(c) != -1 || c == POINT;
    }

    private static boolean hasMark(String s){
        for(int i = 0; i < s.length(); ++i){
            if(isMark(s.charAt(i))){
                return true;
            }
        }
        return false;
    }

    protected static boolean isPureType(String s){
        if(null == s || s.length() <= 0){
            return false;
        }

        int type = indexOfNumber(s.charAt(0));
        if(type < 0){
            return false;
        }
        for(int j = 1; j < s.length(); ++j){
            char c = s.charAt(j);
            if(indexOfNumber(c) != type && !isConnector(c) && !isMark(c)){
                return false;
            }
        }
        return true;
    }

    protected static boolean looksLikeArabic(char c){
        return looksLikeNumber(c) && NUMBERS_CN.indexOf(c) == -1 && NUMBERS_TW.indexOf(c) == -1;
    }

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Numbers";
	}

	@Override
	public void readDef(DataInputStream dataIn) throws IOException {
		// TODO Auto-generated method stub
		dNames.clear();
		int cnt = dataIn.readInt();
		for(int i = 0; i < cnt; i++)
		{
			String str = "";
        	char c;
        	while((c = dataIn.readChar())!='\n')
        	{
        		str += c;
        	}
        	dNames.add(str);
		}
		
		mCounts = new int[subClassCount()];		
	}

	@Override
	public void writeDef(DataOutputStream dataOut) throws IOException {
		// TODO Auto-generated method stub
		dataOut.writeInt(dNames.size());
		for(int i = 0; i < dNames.size(); i++)
		{
			dataOut.writeChars(dNames.get(i)+"\n");
		}
		
	}
}
