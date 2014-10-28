package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Url extends RulePrevious {
    //private static String REGEX_URL1 = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";


	String REGEX_URL="(((http(s?)|HTTP(S?)|ftp|FTP|file|FILE):)?//)?(?<!@([\\w|\\.|-]){0,70})([\\w\\-]+\\.([\\w\\-_]+\\.)*)"
                         + "((biz|com|cn|cc|co|hk|io|im|it|lt|mobi|org|net|us|uk|BIZ|COM|CN|CC|CO|HK|IO|IM|IT|LT|MOBI|ORG|NET|US|UK)"
                         + "|((25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})"
                         + "(?=(:\\d{1,4})?(/[\\.\\,\\;\\?\\'\\\\+&%\\$#\\=~\\-\\w]+)+/?))(:\\d{1,4})?(/\\w+)*?(/[\\.\\,\\;\\?\\'\\\\+&%\\$#\\=~\\-\\w]+)*/?";


    private static String REGEX_IP_URL="(((http(s?)|HTTP(S?)|ftp|FTP|file|FILE):)?//)?((25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})"
                      +"(?=(:\\d{1,5})?(/[\\.\\,\\;\\?\\'\\\\+&%\\$#\\=~\\-\\w]+)+/?)(:\\d{1,5})?(/\\w+)*?(/[\\.\\,\\;\\?\\'\\\\+&%\\$#\\=~\\-\\w]+)*/?";

    private Pattern mPattern;
    private Pattern mPatternIP;
    private List<String> curUrls = new ArrayList<String>();

    public Url(){
        mPattern = Pattern.compile(REGEX_URL);
        mPatternIP = Pattern.compile(REGEX_IP_URL);
    }


    public void reset(){
        curUrls.clear();
    }

    public void process(Corpus cps) {
        List<String> ret1 = new ArrayList<String>();
        for (String line: cps.getRefinedSegments()) {
            Matcher matcher = mPattern.matcher(line);
            List<String> ret = new ArrayList<String>();
            int lastEnd = 0;
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                if (start > lastEnd) {
                    ret.add(line.substring(lastEnd, start));
                }
//            System.out.println("URL:" + str.substring(start, end));
                curUrls.add(line.substring(start, end));
                lastEnd = end;
            }
            if (lastEnd < line.length() - 1) {
                ret.add(line.substring(lastEnd));
            }


            for (String s : ret) {
                matcher = mPatternIP.matcher(s);
                lastEnd = 0;
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    if (start > lastEnd) {
                        ret1.add(s.substring(lastEnd, start));
                    }
//                System.out.println("URL_IP:" + s.substring(start, end));
                    curUrls.add(s.substring(start, end));
                    lastEnd = end;
                }
                if (lastEnd < s.length() - 1) {
                    ret1.add(s.substring(lastEnd));
                }
            }
        }
        cps.setRefinedSegments(ret1);
        cps.getX()[this.getStartIndex()] = curUrls.size() > 0 ? 1 : 0;
    }
    
    public static void main(String[] args) {
        String[] strs = {//"积分攒不停，一起分豪礼”活动你还没参加吗？已经累计送出13部华为智能手机、10张超值加油卡和N张话费流量卡咯！后面还有IPhone5、SONY录音笔等豪礼等你拿，兑奖、砸蛋和秒杀！还在等什么？快来参加吧！\thttp://101.227.251.70:8081/ds.jsp?ms=b35000247b35000038a25000051\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                //"积分攒不停，一起分豪礼”活动你还没参加吗？已经累计送出13部华为智能手机、加吧！\thttp://101.227.251.70:8081/ds.jsp?ms=b35000247b35000038a25000051 三地方看见啊塑料袋分开",
                "【富阳麻将】网络大赛火热进行中，你来打麻将，我就送话费！参赛就有奖！免费下载www.0571qp.com询4000990330【本地游】",
        };
    }

    @Override
    public int subClassCount() {
        return 1;
    }

    @Override
    public void train(List<Corpus> cpss) {

    }

    @Override
	public String getName() {
		return "URL";
	}


	@Override
	public void readDef(DataInputStream dataIn) throws IOException {

	}


	@Override
	public void writeDef(DataOutputStream dataOut) throws IOException {

	}
}
