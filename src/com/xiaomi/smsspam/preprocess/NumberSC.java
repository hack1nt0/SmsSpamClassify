package com.xiaomi.smsspam.preprocess;

public class NumberSC extends Numbers{

	static interface ASCII extends Numbers.SubClass{
        
    }

    static interface UTF extends Numbers.SubClass{
        
    }

    static interface PureASCII extends Numbers.SubClass{
        
    }

    static interface PureUTF extends Numbers.SubClass{
        
    }

    static class BankCard implements Numbers.SubClass {
        @Override
        public boolean fit(String number) {
            int c = 0;
            for (int i = 0; i < number.length(); ++i) {
                if (looksLikeNumber(number.charAt(i))) {
                    c++;
                }
            }
            for (int i = 0; i < BANK_CARD_COUNT.length; ++i) {
                if (c == BANK_CARD_COUNT[i]) {
                    // System.out.println("Bank card:" + s);
                    return true;
                }
            }
            return false;
        }
    }

    static class Phone implements Numbers.SubClass {
        @Override
        public boolean fit(String number) {
            boolean firstNumberGot = false;
            char firstNumber = '1';
            int c = 0;
            for(int i = 0; i < number.length(); ++i){
                if(looksLikeNumber(number.charAt(i))){
                    if(!firstNumberGot){
                        firstNumberGot = true;
                        firstNumber = number.charAt(i);
                    }
                    c++;
                }
            }
            if(c > 5 && !isPureType(number)){
                //if(CHARS_LOOK_LIKE_ONE.indexOf(firstNumber) != -1){
                    //System.out.println("ConfusionMPhNumbers:" + s);
                    return true;
                //}
            }
            return false;
        
        }
    }

    static class MobilePhone implements Numbers.SubClass {
        protected static final int MOBILE_PHONE_LENGTH = 11;
        protected static final String CHARS_LOOK_LIKE_ONE = "1１一壹I|l｜ｌＩ";
        @Override
        public boolean fit(String number) {
            boolean firstNumberGot = false;
            char firstNumber = '1';
            int c = 0;
            for(int i = 0; i < number.length(); ++i){
                if(looksLikeNumber(number.charAt(i))){
                    if(!firstNumberGot){
                        firstNumberGot = true;
                        firstNumber = number.charAt(i);
                    }
                    c++;
                }
            }
            if(MOBILE_PHONE_LENGTH == c && isPureType(number)){
                if(firstNumber == '1' || '１' == firstNumber || '一' == firstNumber || '壹' == firstNumber){
                    //System.out.println("MobilePhoneNumber:" + s);
                    return true;
                }
            }
            return false;
        }
    }

    static class Others implements Numbers.SubClass{
        public boolean fit(String number) {
            return true;
        }
    }
}