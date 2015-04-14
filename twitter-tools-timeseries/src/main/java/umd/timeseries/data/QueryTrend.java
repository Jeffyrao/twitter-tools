package umd.timeseries.data;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

public class QueryTrend
{
	public int qid;
	public String query;
	public String queryTime;
	public List<Integer> relCounts;
	public List<Integer> tweetCounts;
	public List<Double> relativeRelCnts;

	public QueryTrend( int qid, int[] hist, int[] tweetCnts)
	{
		this.qid = qid;
		this.relCounts = Lists.newArrayList( Ints.asList( hist ) );
		this.tweetCounts = Lists.newArrayList( Ints.asList( tweetCnts ) );
		this.relativeRelCnts = new ArrayList<Double>();
		for (int i = 0; i < relCounts.size(); i++) {
			relativeRelCnts.add((double)(relCounts.get(i)+0.1)/(tweetCounts.get(i)+0.1));
		}
	}
	
	public void setQueryInfo(String query, String queryTime)
	{
		this.query = query;
		this.queryTime = queryTime;
	}
}
