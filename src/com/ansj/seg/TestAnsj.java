package com.ansj.seg;

import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.Collection;
import java.util.List;

/**
 * Created by root on 14-8-20.
 */
public class TestAnsj {
    public static void main(String[] args) {
        String data = "第０６６期号已公布。登陆WWW.k278.COM\t看。用户名:181638\t咨询电话：18760635599\t沈小姐";
        List<Term> parse = ToAnalysis.parse(data);
        System.out.println(parse);
        KeyWordComputer kwc = new KeyWordComputer(5);
        String title = "";
        String content = "低至五折风暴！9月底前指定日刷广发信用卡五十余城市享千余家商户优惠，点\twww.cgbchina.com.cn/hd/bj\t询【广发银行]";
        Collection<Keyword> result = kwc.computeArticleTfidf(title, content);
        System.out.println(result);
    }
}
