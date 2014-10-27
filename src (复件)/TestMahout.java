import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.AveragingPreferenceInferrer;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.nio.Buffer;
import java.util.List;

/**
 * Created by root on 14-9-2.
 */
public class TestMahout {
    public static void main(String[] args) throws IOException, TasteException {
        // 1. 选择数据源
// 数据源格式为UserID,MovieID,Ratings
// 使用文件型数据接口

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/ml-10M100K/ratings.dat")));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/ml-10M100K/ratings.txt")));
        String line = in.readLine();
        while (line != null) {
            String tmp[] = line.split("::");
            out.write(tmp[0] + "," + tmp[1] + "," + tmp[2]);
            out.write("\n");
            line = in.readLine();
        }
        out.close();

        DataModel model = new FileDataModel(new File("data/ml-10M100K/ratings.txt"));

// 2. 实现相似度算法
// 使用PearsonCorrelationSimilarity实现UserSimilarity接口, 计算用户的相似度
// 其中PearsonCorrelationSimilarity是基于皮尔逊相关系数计算相似度的实现类
// 其它的还包括
// EuclideanDistanceSimilarity：基于欧几里德距离计算相似度
// TanimotoCoefficientSimilarity：基于 Tanimoto 系数计算相似度
// UncerteredCosineSimilarity：计算 Cosine 相似度
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
// 可选项
        similarity.setPreferenceInferrer(new AveragingPreferenceInferrer(model));

// 3. 选择邻居用户
// 使用NearestNUserNeighborhood实现UserNeighborhood接口, 选择最相似的三个用户
// 选择邻居用户可以基于'对每个用户取固定数量N个最近邻居'和'对每个用户基于一定的限制，取落在相似度限制以内的所有用户为邻居'
// 其中NearestNUserNeighborhood即基于固定数量求最近邻居的实现类
// 基于相似度限制的实现是ThresholdUserNeighborhood
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(3, similarity, model);

// 4. 实现推荐引擎
// 使用GenericUserBasedRecommender实现Recommender接口, 基于用户相似度进行推荐
        Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        Recommender cachingRecommender = new CachingRecommender(recommender);
        List<RecommendedItem> recommendations = cachingRecommender.recommend(1234, 10);

// 输出推荐结果
        for (RecommendedItem item : recommendations) {
            System.out.println(item.getItemID() + "\t" + item.getValue());
        }
    }
}
