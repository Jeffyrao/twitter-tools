package umd.timeseries.regression;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Table;

import umd.timeseries.data.QueryTrendSet;
import umd.timeseries.data.TopicTrend;
import umd.timeseries.data.TopicTrendSet;
import umd.timeseries.utils.ExtractWordCountFromJson;
import umd.timeseries.utils.KDEData;
import umd.twittertools.data.TweetSet;
import umd.twittertools.run.RunTemporalModel;

public class FeatureGenerator
{
//	public static final int interval = 3600;	// 1 slots for 1 hour
//	public static final int interval = 21600;	// 4 slots for 1 day
	public static final int interval = 86400;	// 1 slots for 1 day
//	private static final int sample_n = 17;
	
	// 1112
/*	private static final int max_second = 17 * 24 * 60 * 60;
	private static final int unigram_n = 10;
	private static final int bigram_n = 10;
*/	
	// 1314
	private static final int max_second = 17 * 24 * 60 * 60;
	private static final int unigram_n = 10;
	private static final int bigram_n = 100;
	
	public static void main( String[] args ) throws IOException
	{
		int year = 2011;
		String searchResultFile = "data/ql.2011.total.txt";
		String qrelsFile = "data/qrels.2011.total.txt";
		String unigramFile = "data/unigram_trend_5min_1112.txt";
		String bigramFile = "data/bigram_trend_5min_1112.txt";
		String featureFile = "data/features_regression_1112.txt";
		
		/*int year = 2013;
		String searchResultFile = "data/ql.2013.total.txt";
		String qrelsFile = "data/qrels.2013.total.txt";
		String unigramFile = "data/unigram_trend_5min_1314.txt";
		String bigramFile = "data/bigram_trend_5min_1314.txt";
		String featureFile = "data/features_regression_1314.txt";*/
		
		Map<Integer, TweetSet> query2TweetSet = TweetSet.fromFile(searchResultFile);
		Table<Integer, Long, Integer> qrels = RunTemporalModel.loadGroundTruth(qrelsFile);
		TopicTrendSet unigramTrends = ExtractWordCountFromJson.read(unigramFile, false, year, false);
		TopicTrendSet bigramTrends = ExtractWordCountFromJson.read(bigramFile, true, year, false);
		TopicTrendSet mergeTrends = TopicTrendSet.merge(unigramTrends, bigramTrends);
		mergeTrends.computeEntropy();
		
		QueryTrendSet queryTrendSet = new QueryTrendSet( max_second, interval );
		queryTrendSet.init( query2TweetSet, qrels, unigramTrends );
		
		BufferedWriter bw = new BufferedWriter( new FileWriter( featureFile ) );
		
		int historyLength = 0;
		int numberOfQueries = 0;
		Iterator<TopicTrend> iter = mergeTrends.iterator();
		while( iter.hasNext() )
		{
			TopicTrend topic = iter.next();
			
			if( !qrels.containsRow( topic.id ) || topic.id == 90 || topic.id == 122)
			{
				System.out.println( topic.id );
				continue;
			}
			
			Map<String,double[]> uniDistributionMap = new HashMap<String, double[]>();
			for( String term : topic.unigramCounts.keySet() )
			{
				List<Integer> termCounts = topic.unigramCounts.get( term );
				historyLength = termCounts.size();
				KDEData data = new KDEData( termCounts, false);
				if( !data.isValid() )
					continue;
				uniDistributionMap.put( term, data.getKDEWeights() );
//				uniDistributionMap.put( term, weights );
			}

			Map<String, double[]> biDistributionMap = new HashMap<String, double[]>();
			for( String term : topic.bigramCounts.keySet() )
			{
				List<Integer> termCounts = topic.bigramCounts.get( term );
				KDEData data = new KDEData( termCounts, false);
				if( !data.isValid() )
					continue;
				biDistributionMap.put( term, data.getKDEWeights() );
//				biDistributionMap.put( term, weights );
			}

			List<Integer> queryCounts = queryTrendSet.getQueryTrend( topic.id ).relCounts;
			queryCounts = queryCounts.subList( 0, historyLength );
			KDEData data = new KDEData( queryCounts, true);
			//if( !data.isValid() )
			//	continue;
			
			for( int i = 0; i < historyLength; ++i )
			{
//				bw.write( topic.id + " " + weights[i] + " " );
				//if (!uniDistributionMap.isEmpty() && !biDistributionMap.isEmpty()){
				bw.write( topic.id + " " + data.getKDEWeights()[i] + " " );
				int n = unigram_n + bigram_n;
				String[] features = new String[n*2];
				for( int j = 0; j < n; ++j )
				{
					features[j] = "0 ";
					features[n+j] = "1 ";
				}
				
				int j = 0;
				if (!uniDistributionMap.isEmpty()) {
					for( String term : uniDistributionMap.keySet() )
					{
						features[j] = (topic.unigramEntropy.get( term ) - topic.MIN_ENTROPY) + " "; //problematic
						features[n+j] = uniDistributionMap.get( term )[i] + " ";
						++j;
					}
				}
				
				j = n - bigram_n;
				if (!biDistributionMap.isEmpty()) {
					for( String term : biDistributionMap.keySet() )
					{
						features[j] = (topic.bigramEntropy.get( term ) - topic.MIN_ENTROPY) + " "; //problematic
						features[n+j] = biDistributionMap.get( term )[i] + " ";
						++j;
					}
				}
				for( String f : features )
				{
					bw.write( f );
				}
				bw.write( "\n" );
			}//}
			numberOfQueries++;
		}
		System.out.println(numberOfQueries);
		bw.close();
	}
}
