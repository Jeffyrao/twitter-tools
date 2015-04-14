package umd.timeseries.regression;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import umd.timeseries.data.QueryTrendSet;
import umd.timeseries.data.TopicTrend;
import umd.timeseries.data.TopicTrendSet;
import umd.timeseries.utils.ExtractWordCountFromJson;
import umd.timeseries.utils.KDEData;
import umd.twittertools.data.TweetSet;
import umd.twittertools.run.RunTemporalModel;

import com.google.common.collect.Table;

public class Visualization
{
	private static final int interval = 3600;	// 1 slots for 1 hour
//	private static final int interval = 21600;	// 4 slots for 1 day
//	private static final int interval = 86400;	// 1 slots for 1 day
	
	public static void main( String[] args ) throws IOException
	{		
//		double e1 = 1.1664;
//		double e2 = 0.9135;
		
		// 1112
		int max_second = 17 * 24 * 60 * 60;
		double e1 = -2.2507;
		double e2 = -2.3784;
		int year = 2011;
		String searchResultFile = "data/ql.2011.total.txt";
		String qrelsFile = "data/qrels.2011.total.txt";
		String unigramFile = "data/unigram_trend_5min_1112.txt";
		String bigramFile = "data/bigram_trend_5min_1112.txt";
		String visFile = "data/vis_Regression_1112.txt";
	
		// 1314
//		int max_second = 59 * 24 * 60 * 60;
//		double e1 = -1.5446;
//		double e2 = -1.0278;
//		int year = 2013;
//		String searchResultFile = "data/ql.2013.total.txt";
//		String qrelsFile = "data/qrels.2013.total.txt";
//		String unigramFile = "data/unigram_trend_5min_1314.txt";
//		String bigramFile = "data/bigram_trend_5min_1314_v2.txt";
//		String visFile = "data/vis_Regression_1314.txt";
		
		Map<Integer, TweetSet> query2TweetSet = TweetSet.fromFile(searchResultFile);
		Table<Integer, Long, Integer> qrels = RunTemporalModel.loadGroundTruth(qrelsFile);
		TopicTrendSet unigramTrends = ExtractWordCountFromJson.read(unigramFile, false, year);
		TopicTrendSet bigramTrends = ExtractWordCountFromJson.read(bigramFile, true, year);
		TopicTrendSet mergeTrends = TopicTrendSet.merge(unigramTrends, bigramTrends);
		mergeTrends.computeEntropy();
		
		QueryTrendSet queryTrendSet = new QueryTrendSet( max_second, interval );
		queryTrendSet.init( query2TweetSet, qrels, unigramTrends );
		
		BufferedWriter bw = new BufferedWriter( new FileWriter( visFile ) );

		Iterator<TopicTrend> iter = mergeTrends.iterator();
		while( iter.hasNext() )
		{
			TopicTrend topic = iter.next();

			if( !qrels.containsRow( topic.id ) )
			{
				System.out.println( topic.id );
				continue;
			}
			
			KDEData data;
			int historyLength = 0;
			double weightSum = 0;
			List<Double> weights = new ArrayList<Double>();
			List<double[]> dists = new ArrayList<double[]>();
			List<String> terms = new ArrayList<String>();
			List<Double> entropies = new ArrayList<Double>();
			List<List<Integer>> counts = new ArrayList<List<Integer>>();
			
			for( String term : topic.unigramCounts.keySet() )
			{
				List<Integer> termCounts = topic.unigramCounts.get( term );
				historyLength = termCounts.size();
				data = new KDEData( termCounts );
				if( data.isValid() )
				{
					dists.add( data.getKDEWeights() );
					double entropy = -topic.unigramEntropy.get( term );
					double weight = Math.pow( entropy, e1 );
	//				double weight = Math.pow( e1, -entropy );
					weightSum += weight;
					weights.add( weight );
					terms.add( term );
					entropies.add( entropy );
					counts.add( termCounts );
				}
			}
			for( String term : topic.bigramCounts.keySet() )
			{
				List<Integer> termCounts = topic.bigramCounts.get( term );
				data = new KDEData( termCounts );
				if( data.isValid() )
				{
					dists.add( data.getKDEWeights() );
					double entropy = -topic.bigramEntropy.get( term );
					double weight = Math.pow( entropy, e2 );
	//				double weight = Math.pow( e2, -entropy );
					weightSum += weight;
					weights.add( weight );
					terms.add( term );
					entropies.add( entropy );
					counts.add( termCounts );
				}
			}
			
			bw.write( "\"" + topic.query.replaceAll( "\"", "" ) + "\" " + topic.id + " " );
			List<Integer> queryCounts = queryTrendSet.getQueryTrend( topic.id ).relCounts;
			queryCounts.subList( 0, historyLength );
			for( int count : queryCounts )
			{
				bw.write( count + " " );
			}
			bw.write( "\n" );
			
			data = new KDEData( queryCounts );
			if( !data.isValid() )
				continue;
			bw.write( "KDE " + topic.id + " " );
			for( double value : data.getKDEWeights() )
			{
				bw.write( value + " " );
			}
			bw.write( "\n" );
			
			bw.write( "estimate " + topic.id + " " );
			for( int i = 0; i < historyLength; ++i )
			{
				double value = 0;
				for( int j = 0; j < weights.size(); ++j )
				{
					value += dists.get( j )[i] * weights.get( j ) / weightSum;
//					value += dists.get( j )[i] * weights.get( j ) / weights.size();
				}
				bw.write( value + " " );
			}
			bw.write( "\n" );
			
			for( int i = 0; i < terms.size(); ++i )
			{
				bw.write(  "\"" + terms.get( i ) + " (" + weights.get( i )/weightSum + ", " + entropies.get( i ) + ")\" " + topic.id + " " );
//				bw.write(  "\"" + terms.get( i ) + " (" + weights.get( i )/weights.size() + ", " + entropies.get( i ) + ")\" " + topic.id + " " );
				for( double d : dists.get( i ) )
				{
					bw.write( d + " " );
				}
				bw.write( "\n" );
			}
		}
		
		bw.close();
	}
}
