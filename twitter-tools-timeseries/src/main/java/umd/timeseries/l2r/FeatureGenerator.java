package umd.timeseries.l2r;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.apple.crypto.provider.Debug;
import com.google.common.collect.Table;
import com.google.common.primitives.Doubles;

import umd.timeseries.data.TopicTrend;
import umd.timeseries.data.TopicTrendSet;
import umd.timeseries.utils.ExtractWordCountFromJson;
import umd.twittertools.data.Tweet;
import umd.twittertools.data.TweetSet;
import umd.twittertools.kde.Data;
import umd.twittertools.kde.WeightKDE;
import umd.twittertools.model.Model;
import umd.twittertools.run.RunTemporalModel;
import umontreal.iro.lecuyer.probdist.NormalDist;

public class FeatureGenerator {
	
	public static final double AMPLIFY_ALPHA = 1.0;
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String searchResultFile = args[0];
		String qrelsFile = args[1];
		String unigramFile = args[2];
		String bigramFile = args[3];
		String featureFile = args[4];
		int year = Integer.parseInt(args[5]);
		Map<Integer, TweetSet> query2TweetSet = TweetSet.fromFile(searchResultFile);
		Table<Integer, Long, Integer> qrels = RunTemporalModel.loadGroundTruth(qrelsFile);
		TopicTrendSet unigramTrends = ExtractWordCountFromJson.read(unigramFile, false, year);
		TopicTrendSet bigramTrends = ExtractWordCountFromJson.read(bigramFile, true, year);
		TopicTrendSet mergeTrends = TopicTrendSet.merge(unigramTrends, bigramTrends);
		mergeTrends.computeEntropy();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(featureFile));
		
		Iterator<TopicTrend> iter = mergeTrends.iterator();
		while (iter.hasNext()) {
			TopicTrend topic = iter.next();
			TweetSet tweetSet = query2TweetSet.get(topic.id);
			
			if (!qrels.containsRow(topic.id) || tweetSet == null) {
				System.out.println(topic.id);
				continue;
			}
			
			double sum = 0;
			double maxUnigramEntropy = 0;
			double[] data, weights;
			Data unigramData = null;
			NormalDist kern = new NormalDist();
			// prepare unigram statistics
			if (topic.maxUnigram != null) {
				List<Integer> unigramCounts = topic.getUnigramCount(topic.maxUnigram);
				maxUnigramEntropy = topic.unigramEntropy.get(topic.maxUnigram) - topic.MIN_ENTROPY;
				for (int count: unigramCounts) {
					// sum += count;
					sum += Math.pow(count, AMPLIFY_ALPHA);
				}
				data = new double[unigramCounts.size()];
				weights = new double[unigramCounts.size()];
				for (int i = 0; i < weights.length; i++) {
					data[i] = i * (TopicTrendSet.INTERVAL * 1.0 / 1440);
					// weights[i] = unigramCounts.get(i) / sum;
					weights[i] = Math.pow(unigramCounts.get(i), AMPLIFY_ALPHA) / sum;
				}
				unigramData = new Data(data, weights);
				unigramData.computeStatistics();
			}
			
			// prepare bigram statistics
			double maxBigramEntropy = 0;
			Data bigramData = null;
			if (topic.maxBigram != null) {
				List<Integer> bigramCounts = topic.getBigramCount(topic.maxBigram);
				maxBigramEntropy = topic.bigramEntropy.get(topic.maxBigram) - topic.MIN_ENTROPY;
				sum = 0;
				for (int count: bigramCounts) {
					//sum += count;
					sum += Math.pow(count, AMPLIFY_ALPHA);
				}
				data = new double[bigramCounts.size()];
				weights = new double[bigramCounts.size()];
				for (int i = 0; i < weights.length; i++) {
					data[i] = i * (TopicTrendSet.INTERVAL * 1.0 / 1440);
					//weights[i] = bigramCounts.get(i) / sum;
					weights[i] = Math.pow(bigramCounts.get(i), AMPLIFY_ALPHA) / sum;
				}
				bigramData = new Data(data, weights);
				bigramData.computeStatistics();
			}
			
			// get unigram and bigram features
			for (Tweet tweet: tweetSet) {
				double tweetTime = tweet.getTimeDiff() * 1.0f / Model.TIME_INTERVAL;
				double unigramDensity = 0, bigramDensity = 0; 
				if (topic.maxUnigram != null) {
					unigramDensity = WeightKDE.computeDensity(unigramData, kern, tweetTime);
				}
				if (topic.maxBigram != null) {
					bigramDensity = WeightKDE.computeDensity(bigramData, kern, tweetTime);
				}
				int label = qrels.contains(topic.id, tweet.getId()) ? 1 : 0;
				double unigramLogDensity = (unigramDensity == 0) ? 0 : Math.log(unigramDensity);
				double bigramLogDensity = (bigramDensity == 0) ? 0 : Math.log(bigramDensity);
				double unigramProduct = unigramDensity * maxUnigramEntropy;
				double bigramProduct = bigramDensity * maxBigramEntropy;
				bw.write(String.format("%d qid:%d 1:%.7f 2:%.7f 3:%.7f 4:%.7f 5:%.7f 6:%.7f 7:%.7f # %d\n", label, 
						topic.id, tweet.getQlScore(), maxUnigramEntropy, unigramLogDensity, 
						maxBigramEntropy, bigramLogDensity, unigramProduct, bigramProduct, tweet.getId()));
			}
		}
		
		bw.close();
	}

}
