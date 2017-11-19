package org.lenskit.mooc;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.lenskit.LenskitRecommender;
import org.lenskit.api.Recommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.Entity;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricColumn;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;
import org.lenskit.eval.traintest.recommend.TopNMetric;
import org.lenskit.mooc.cbf.TagData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Metric that measures how long a TopN list actually is.
 */
public class TagEntropyMetric extends TopNMetric<TagEntropyMetric.Context> {
    private static final Logger logger = LoggerFactory.getLogger(TagEntropyMetric.class);

    /**
     * Construct a new tag entropy metric metric.
     */
    public TagEntropyMetric() {
        super(TagEntropyResult.class, TagEntropyResult.class);
    }

    @Nonnull
    @Override
    public MetricResult measureUser(TestUser user, int expectedSize, ResultList recommendations, Context context) {
        if (recommendations == null || recommendations.isEmpty()) {
            return MetricResult.empty();
            // no results for this user.
        }
        int n = recommendations.size();

        // get tag data from the context so we can use it
        DataAccessObject dao = context.getDAO();
        double entropy = 0.0;

        // TODO Compute the entropy of the movie list
        // You can get a movie's tags with:
        // dao.query(TagData.ITEM_TAG_TYPE).withAttribute(TagData.ITEM_ID, res.getId()).get();
        // Each entity's tag can be retrieved with 'itemTag.get(TagData.TAG)'

        // add all tags to a set
        HashSet <String> tagSet = new HashSet<>();
        Map<Long, Map<String, Double>> mymap = new HashMap<>();
        Map<Long, Double> cntMap = new HashMap<>();
        for (Result res: recommendations) {
            Map<String, Double> tempMap = new HashMap<>();
            Double cnt=0.0;
            for (Entity itemTag: dao.query(TagData.ITEM_TAG_TYPE).withAttribute(TagData.ITEM_ID, res.getId()).get()) {
                String tagName = itemTag.get(TagData.TAG);
                tagSet.add(tagName);
                if(tempMap.containsKey(tagName))
                    tempMap.put(tagName , tempMap.get(tagName)+1);
                else
                    tempMap.put(tagName,1.0);
                cnt++;
            }
            mymap.put(res.getId(), tempMap);
            cntMap.put(res.getId(), cnt);
        }

        // foreach tag, foreach movie, calculate H(L)
        for (String tag: tagSet) {
            Double ans=0.0;
            for (Result res: recommendations) {
                Double cnt = 0.0, total;
                Map<String, Double> tempMap = mymap.get(res.getId());
                if (tempMap.containsKey(tag))
                    cnt += tempMap.get(tag);
                total = cntMap.get(res.getId());
                ans += (cnt / total);
            }
            ans=ans/n;
            entropy = entropy + (-ans)*(Math.log(ans)/Math.log(2));
        }
        context.addUser(entropy);
        return new TagEntropyResult(entropy);
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, Recommender recommender) {
        return new Context((LenskitRecommender) recommender);
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Context context) {
        return new TagEntropyResult(context.getMeanEntropy());
    }

    public static class TagEntropyResult extends TypedMetricResult {
        @MetricColumn("TopN.TagEntropy")
        public final Double entropy;

        public TagEntropyResult(double ent) {
            entropy = ent;
        }

    }

    public static class Context {
        private LenskitRecommender recommender;
        private double totalEntropy;
        private int userCount;

        /**
         * Create a new context for evaluating a particular recommender.
         *
         * @param rec The recommender being evaluated.
         */
        public Context(LenskitRecommender rec) {
            recommender = rec;
        }

        /**
         * Get the recommender being evaluated.
         *
         * @return The recommender being evaluated.
         */
        public LenskitRecommender getRecommender() {
            return recommender;
        }

        /**
         * Get the DAO for the current recommender evaluation.
         */
        public DataAccessObject getDAO() {
            return recommender.get(DataAccessObject.class);
        }

        /**
         * Add the entropy for a user to this context.
         *
         * @param entropy The entropy for one user.
         */
        public void addUser(double entropy) {
            totalEntropy += entropy;
            userCount += 1;
        }

        /**
         * Get the average entropy over all users.
         *
         * @return The average entropy over all users.
         */
        public double getMeanEntropy() {
            return totalEntropy / userCount;
        }
    }
}
