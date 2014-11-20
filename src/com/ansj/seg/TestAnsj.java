package com.ansj.seg;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.List;

/**
 * Created by root on 14-8-20.
 */
public class TestAnsj {
    public static void main(String[] args) {

        String data = "第０６６期号已公布。登陆WWW.k278.COM\t看。用户名:181638\t咨询电话：18760635599\t沈小姐";
        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        List<Term> parse = BaseAnalysis.parse(data);
        long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double usedMB = (after - before) / 1024.0 / 1024;
        System.out.println(parse);
        System.out.println(usedMB);

        /*
        KeyWordComputer kwc = new KeyWordComputer(5);
        String title = "";
        String content = "低至五折风暴！9月底前指定日刷广发信用卡五十余城市享千余家商户优惠，点\twww.cgbchina.com.cn/hd/bj\t询【广发银行]";
        Collection<Keyword> result = kwc.computeArticleTfidf(title, content);
        System.out.println(result);*/
    }
}
