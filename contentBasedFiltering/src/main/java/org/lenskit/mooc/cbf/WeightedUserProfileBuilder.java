package org.lenskit.mooc.cbf;

import org.lenskit.data.ratings.Rating;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Build a user profile from all positive ratings.
 */
public class WeightedUserProfileBuilder implements UserProfileBuilder {
    /**
     * The tag model, to get item tag vectors.
     */
    private final TFIDFModel model;

    @Inject
    public WeightedUserProfileBuilder(TFIDFModel m) {
        model = m;
    }

    @Override
    public Map<String, Double> makeUserProfile(@Nonnull List<Rating> ratings) {
        // Create a new vector over tags to accumulate the user profile
        Map<String,Double> profile = new HashMap<>();

        double meanRating = 0.0;
        for (Rating r: ratings) {
            meanRating += r.getValue();
        }
        meanRating /= ratings.size();

        // Iterate over the user's ratings to build their profile
        for (Rating r: ratings) {
            //if (r.getValue() >= RATING_THRESHOLD) {
                long itemId = r.getItemId();
                Map<String, Double> itemVector = model.getItemVector(itemId);

                for(Map.Entry<String, Double> vec : itemVector.entrySet()) {
                    double val = profile.containsKey(vec.getKey())?profile.get(vec.getKey()):0.0;
                    val += (r.getValue()-meanRating)*(vec.getValue());
                    profile.put(vec.getKey(), val);
                }

                // TODO Get this item's vector and add it to the user's profile
           // }
        }
        // TODO Normalize the user's ratings
        // TODO Build the user's weighted profile


        // The profile is accumulated, return it.
        return profile;
    }
}
