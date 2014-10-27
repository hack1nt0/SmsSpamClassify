package CFRS;

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
        /*
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
*/

        DataModel model = new FileDataModel(new File("data/ml-10M100K/ratings.txt"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(3, similarity, model);
        Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        List<RecommendedItem> recommendations = recommender.recommend(1234, 10);
        for (RecommendedItem item : recommendations) {
            System.out.println(item);
        }
    }
}
