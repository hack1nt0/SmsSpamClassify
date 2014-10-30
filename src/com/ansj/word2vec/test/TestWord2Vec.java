package com.ansj.word2vec.test;
import java.io.*;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import com.ansj.word2vec.Learn;
import com.ansj.word2vec.Word2VEC;
import org.json.JSONException;
import org.json.JSONObject;

public class TestWord2Vec {
    private static final File corpus = new File("data/trainning_data.txt");
    private static final File w2CIn = new File("data/w2c.modelIn");
    private static final File w2CModel = new File("data/w2c.model");
    public static void main(String[] args) throws IOException, JSONException {

        if (!w2CModel.canRead()) {

            //构建语料
            parserFile(new FileOutputStream(w2CIn), corpus);

            //进行分词训练
            Learn lean = new Learn();
            lean.learnFile(w2CIn);
            lean.saveModel(w2CModel);
        }


        //加载测试

        Word2VEC w2v = new Word2VEC() ;

        w2v.loadJavaModel(w2CModel) ;

        System.out.println(w2v.distance("代办"));

    }

    private static void parserFile(FileOutputStream out, File file) throws FileNotFoundException,
            IOException, JSONException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String tmpLine = null;
        while ((tmpLine = in.readLine())!= null) {
            parseStr(out, new JSONObject(tmpLine).getString("body"));
        }
        in.close();

    }

    private static void parseStr(FileOutputStream fos, String title) throws IOException {
        List<Term> parse2 = ToAnalysis.parse(title) ;
        StringBuilder sb = new StringBuilder() ;
        for (Term term : parse2) {
            sb.append(term.getName()) ;
            sb.append(" ");
        }
        fos.write(sb.toString().getBytes()) ;
        fos.write("\n".getBytes()) ;
    }
}