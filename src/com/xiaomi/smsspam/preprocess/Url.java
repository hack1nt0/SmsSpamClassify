package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Url extends Rule {
    //private static String REGEX_URL1 = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";


	static String REGEX_URL="(((http(s?)|HTTP(S?)|ftp|FTP|file|FILE):)?//)?(?<!@([\\w|\\.|-]){0,70})([\\w\\-]+\\.([\\w\\-_]+\\.)*)"
                         + "((biz|com|cn|cc|co|hk|io|im|it|lt|mobi|org|net|us|uk|BIZ|COM|CN|CC|CO|HK|IO|IM|IT|LT|MOBI|ORG|NET|US|UK)"
                         + "|((25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})"
                         + "(?=(:\\d{1,4})?(/[\\.\\,\\;\\?\\'\\\\+&%\\$#\\=~\\-\\w]+)+/?))(:\\d{1,4})?(/\\w+)*?(/[\\.\\,\\;\\?\\'\\\\+&%\\$#\\=~\\-\\w]+)*/?";



    private static String REGEX_IP_URL="(((http(s?)|HTTP(S?)|ftp|FTP|file|FILE):)?//)?((25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})"
                      +"(?=(:\\d{1,5})?(/[\\.\\,\\;\\?\\'\\\\+&%\\$#\\=~\\-\\w]+)+/?)(:\\d{1,5})?(/\\w+)*?(/[\\.\\,\\;\\?\\'\\\\+&%\\$#\\=~\\-\\w]+)*/?";

    private static String URL;

    private Pattern mPattern;
    private List<String> URLs;
    private List<String> curURLs;

    private PrintWriter tmpOut, modelOut;

    public Url(){
        curURLs = new ArrayList<>();
        URLs = new ArrayList<>();
        try {
            tmpOut = new PrintWriter(new FileOutputStream("data/extractedURLs.txt"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        String topLevelDomain = "(\\.(aero|" +
                "arpa|" +
                "asia|" +
                "biz|" +
                "cat|" +
                "com|" +
                "coop|" +
                "edu|" +
                "gov|" +
                "int|" +
                "info|" +
                "jobs|" +
                "mil|" +
                "mobi|" +
                "museum|" +
                "name|" +
                "net|" +
                "org|" +
                "pro|" +
                "tel|" +
                "travel|" +
                "xxx|cn|jp|us|uk|im|am))";
        String anySymbolButCN = "[`~!@#$%(\\^)&(\\*)(\\(\\))_(\\+)-=(\\[\\])\\\\(\\{\\})|;':\",(\\.)/<>(\\?)a-zA-Z0-9]";
        String URLPrefix = "((http|https)://)";

        String domainURL = anySymbolButCN + "+" + topLevelDomain+ "+?" + anySymbolButCN + "*";
        domainURL = "(" + domainURL + ")";

        String ipURL= "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])";
        ipURL = URLPrefix + "?" + ipURL + "\\." + ipURL + "\\." + ipURL + "\\." + ipURL + anySymbolButCN + "*";
        ipURL = "(" + ipURL + ")";

        String otherURL = URLPrefix + anySymbolButCN + "*";

        URL = domainURL + "|" + ipURL + "|" + otherURL;

        mPattern = Pattern.compile(URL);
        //mPatternIP = Pattern.compile(REGEX_IP_URL);
    }


    public void reset() {
        URLs.clear();
        curURLs.clear();
    }

    @Override
    public void updRemainingBody(Corpus cps) {
        List<String> nsegs = new ArrayList<String>();
        curURLs.clear();
        for (String line : cps.getRemainingBody()) {
            Matcher matcher = mPattern.matcher(line);
            int lastEnd = 0;
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                if (lastEnd < start) {
                    nsegs.add(line.substring(lastEnd, start));
                }
                curURLs.add(line.substring(start, end));
                lastEnd = end;
            }
            if (lastEnd < line.length()) {
                nsegs.add(line.substring(lastEnd));
            }
        }
        cps.setRemainingBody(nsegs);
        URLs.addAll(curURLs);
    }

    @Override
    public void train(List<Corpus> cpss) {
        for (Corpus cps: cpss)
            updRemainingBody(cps);
        //modelOut = new PrintWriter(new FileOutputStream("data/validURLs.txt"));
    }

    public void process(Corpus cps) {
        updRemainingBody(cps);
        //TODO writeCurRules(extractedRulesOut, curURLs);
        tmpOut.println(curURLs);
        cps.getX()[this.getStartIndex()] = URLs.size() > 0 ? 1 : 0;
    }
    
    public static void main(String[] args) {
        String text = //"积分攒不停，一起分豪礼”活动你还没参加吗？已经累计送出13部华为智能手机、10张超值加油卡和N张话费流量卡咯！后面还有IPhone5、SONY录音笔等豪礼等你拿，兑奖、砸蛋和秒杀！还在等什么？快来参加吧！\thttp://101.227.251.70:8081/ds.jsp?ms=b35000247b35000038a25000051\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
                //"积分攒不停，一起分豪礼”活动你还没参加吗？已经累计送出13部华为智能手机、加吧！\thttp://101.227.251.70:8081/ds.jsp?ms=b35000247b35000038a25000051 三地方看见啊塑料袋分开",
                "【富阳麻将】网络大赛火热进行中，你来打麻将，我就送话费！参赛就有奖！免费下载www.0571qp.com询4000990330【本地游】";
        //String RE = "(http|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#!]*[\\w\\-\\@?^=%&amp;/~\\+#!])?​";

        Pattern automation = Pattern.compile(REGEX_URL);
        Matcher matcher = automation.matcher(text);
        while (matcher.find()) {
            System.out.println((matcher.end() - matcher.start()) + "==? ");
            System.out.println(matcher.group());
        }
    }

    @Override
    public String[] getSubFeatureNames() {
        return new String[]{"has-url"};
    }


    @Override
	public String toString() {
		return "URL";
	}


	@Override
	public void readDef(DataInputStream dataIn) throws IOException {

	}

	@Override
	public void writeDef(DataOutputStream dataOut) throws IOException {

	}
}
