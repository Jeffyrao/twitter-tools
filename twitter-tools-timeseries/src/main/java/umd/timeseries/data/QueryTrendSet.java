package umd.timeseries.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import umd.twittertools.data.Tweet;
import umd.twittertools.data.TweetSet;

import com.google.common.collect.Table;

public class QueryTrendSet
{
	private Map<Integer,QueryTrend> queries = new HashMap<Integer,QueryTrend>();
	
	private int max_second;
	private int interval;

	public QueryTrendSet( int max_second, int interval )
	{
		this.max_second = max_second;
		this.interval = interval;
	}
	
	public void add( int qid, QueryTrend qt )
	{
		queries.put( qid, qt );
	}
	
	public QueryTrend getQueryTrend( int qid )
	{
		return queries.get( qid );
	}
	
	public ArrayList<QueryTrend> getQueryTrends()
	{
		return new ArrayList<QueryTrend>( queries.values() );
	}
	
	public int size()
	{
		return queries.size();
	}
	
	public void init( Map<Integer, TweetSet> query2TweetSet, 
			Table<Integer, Long, Integer> qrels, TopicTrendSet unigramTrends )
	{
		for( Entry<Integer, TweetSet> q2tsp : query2TweetSet.entrySet() )
		{
			int qid = q2tsp.getKey();
			int[] histgram = new int[max_second/interval];
			int[] tweetCnts = new int[max_second/interval];
			Set<Long> tweetids = qrels.row( qid ).keySet();
			for( Tweet tweet : q2tsp.getValue() )
			{	
				tweetCnts[(int)( tweet.getTimeDiff()/interval )]++;
				if( tweetids.contains( tweet.getId() ) )
				{
					if( tweet.getTimeDiff() < 0 )
						continue;
					histgram[(int)( tweet.getTimeDiff()/interval )]++;
				}
			}
			
			add( qid, new QueryTrend( qid, histgram, tweetCnts) );
		}
		
		Iterator<TopicTrend> iter = unigramTrends.iterator();
		while( iter.hasNext() )
		{
			TopicTrend topic = iter.next();
			getQueryTrend( topic.id ).setQueryInfo( topic.query, topic.queryTime );
		}
	}
}
