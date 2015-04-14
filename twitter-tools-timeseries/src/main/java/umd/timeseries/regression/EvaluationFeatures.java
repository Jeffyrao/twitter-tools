package umd.timeseries.regression;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.Table;
import com.google.common.io.Files;

import umd.twittertools.data.Tweet;
import umd.twittertools.data.TweetSet;
import umd.twittertools.run.RunTemporalModel;
import umd.timeseries.data.TopicTrend;
import umd.timeseries.data.TopicTrendSet;
import umd.timeseries.utils.ExtractWordCountFromJson;
import umd.timeseries.utils.KDEData;

public class EvaluationFeatures
{
//	private static final int interval = 3600;	// 1 slots for 1 hour
//	private static final int interval = 21600;	// 4 slots for 1 day
	private static final int interval = 86400;	// 1 slots for 1 day

	public static void main( String[] args ) throws IOException
	{
//		double e1 = 1.1664;
//		double e2 = 0.9135;
		
		// 1112
		//double b1 = 2.8682;
		//double b2 = 2.8337;
		//double b3 = 0.5;
		/*int year = 2011;
		String searchResultFile = "data/ql.2011.total.txt";
		String qrelsFile = "data/qrels.2011.total.txt";
		String unigramFile = "data/unigram_trend_5min_1112.txt";
		String bigramFile = "data/bigram_trend_5min_1112.txt";
		String featureFile = "features/features_Regression_1112.txt";*/
		
		// 1314
//		double e1 = -1.5446;
//		double e2 = -1.0278;
		int year = 2013;
		String searchResultFile = "data/ql.2013.total.txt";
		String qrelsFile = "data/qrels.2013.total.txt";
		String unigramFile = "data/unigram_trend_5min_1314.txt";
		String bigramFile = "data/bigram_trend_5min_1314_v2.txt";
		String featureFile = "features/features_Regression_1314.txt";
		
		Map<Integer, TweetSet> query2TweetSet = TweetSet.fromFile(searchResultFile);
		Table<Integer, Long, Integer> qrels = RunTemporalModel.loadGroundTruth(qrelsFile);
		TopicTrendSet unigramTrends = ExtractWordCountFromJson.read(unigramFile, false, year);
		TopicTrendSet bigramTrends = ExtractWordCountFromJson.read(bigramFile, true, year);
		TopicTrendSet mergeTrends = TopicTrendSet.merge(unigramTrends, bigramTrends);
		mergeTrends.computeEntropy();
		
		// read trained parameter b;
		List<String> lines = Files.readLines(new File("matlab/b_sol_fbs_2013.txt"), Charsets.US_ASCII);
		List<Double> b = new ArrayList<Double>();
		for (String line: lines) {
			b.add(Double.parseDouble(line));
		}
		double b0 = b.get(0);
		double b1 = b.get(1);
		
		
		BufferedWriter bw = new BufferedWriter( new FileWriter( featureFile ) );
		
		Iterator<TopicTrend> iter = mergeTrends.iterator();
		int queryCnt = 0;
		while( iter.hasNext() )
		{
			TopicTrend topic = iter.next();
			double b3 = b.get(queryCnt+2);
			b3 = Math.min(Math.max(b3,0.0),1);
			
			if( !qrels.containsRow( topic.id ) || topic.id == 90 || topic.id == 122)
			{
				System.out.println( topic.id );
				continue;
			}
			
			Map<String,KDEData> termKDEMap = new HashMap<String,KDEData>();
			double unigramSum =0, bigramSum = 0; 
			for( String term : topic.unigramCounts.keySet() )
			{
				unigramSum += Math.pow(topic.unigramEntropy.get(term) - topic.MIN_ENTROPY, 2);
				List<Integer> termCounts = topic.unigramCounts.get( term );
				KDEData data = new KDEData(termCounts, false);
				termKDEMap.put( term, data );
			}
			for( String term : topic.bigramCounts.keySet() )
			{
				bigramSum += Math.pow(topic.bigramEntropy.get(term) - topic.MIN_ENTROPY, 2);
				List<Integer> termCounts = topic.bigramCounts.get( term );
				KDEData data = new KDEData( termCounts, false);
				termKDEMap.put( term, data );
			}
			
			TweetSet ts = query2TweetSet.get( topic.id );
			for( int i = 0; i < ts.size(); ++i )
			{
				Tweet tweet = ts.getTweet( i );
				double timePoint = tweet.getTimeDiff() * 1.0 / interval;
				
				double estDist = 0;
				
				for( String term : topic.unigramCounts.keySet() )
				{	
					KDEData data = termKDEMap.get( term );
					if( data.isValid() )
					{
						double norm_entropy = Math.pow(topic.unigramEntropy.get(term) - topic.MIN_ENTROPY, 2)/unigramSum;
						double weight = Math.pow(Math.E, b0*norm_entropy) - 1; 
						estDist += b3 * data.getKDEWeight(timePoint) * weight;
					}
				}
				
				for( String term : topic.bigramCounts.keySet() )
				{
					
					KDEData data = termKDEMap.get(term);
					if( data.isValid() )
					{
						double norm_entropy = Math.pow(topic.bigramEntropy.get(term)-topic.MIN_ENTROPY,2)/bigramSum;
						double weight = Math.pow(Math.E, b1*norm_entropy) - 1; 
						estDist += (1-b3) * data.getKDEWeight(timePoint) * weight;
					}
				}
				
				int label = qrels.contains(topic.id, tweet.getId()) ? 1 : 0;
				bw.write(String.format("%d qid:%d 1:%.7f 2:%.7f # %d\n", label, topic.id, tweet.getQlScore(), Math.log(estDist), tweet.getId()));
//				bw.write(String.format("%d qid:%d 1:%.7f 2:%.7f # %d\n", label, topic.id, tweet.getQlScore(), estDist, tweet.getId()));
			}
			queryCnt++;
		}
		
		bw.close();
	}
}
