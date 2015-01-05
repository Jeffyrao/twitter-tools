package umd.timeseries.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TopicTrendSet implements Iterable<TopicTrend>{
	private int year;
	private List<TopicTrend> queries = new ArrayList<TopicTrend>();
	
	public static final int THRESHOLD = 10;
	public static float TIME_SPAN;
	public static float MIN_ENTROPY;

	public TopicTrendSet(int year) {
		this.year = year;
		if (this.year == 2011) {
			TIME_SPAN = 18.0f; // 18 days (Tweet 2011 Collection)
		} else {
			TIME_SPAN = 60.0f; // 60 days (Tweet 2013 Collection)
		}
		MIN_ENTROPY = 0;
		for (int i = 0; i < TIME_SPAN; i++) {
			float uniformProb = 1.0f / TIME_SPAN;
			MIN_ENTROPY += uniformProb * Math.log(uniformProb) / Math.log(2);
		}
	}
	
	public void add(TopicTrend q) {
	  queries.add(q);
	}
	  
	public void addAll(TopicTrendSet set) {
	  queries.addAll(set.queries);
	}
	
	@Override
	public Iterator<TopicTrend> iterator() {
	  return queries.iterator();
	}
	
	public void computeEntropy() {
		Iterator<TopicTrend> iter = this.iterator();
		while (iter.hasNext()) {
			iter.next().computeEntropy();
		}
	}
	
	public static TopicTrendSet merge(TopicTrendSet unigramSet, TopicTrendSet bigramSet) {
		TopicTrendSet mergeSet = new TopicTrendSet(unigramSet.year);
		Iterator<TopicTrend> unigramIter = unigramSet.iterator();
		Iterator<TopicTrend> bigramIter = bigramSet.iterator();
		while(unigramIter.hasNext() && bigramIter.hasNext()) {
			TopicTrend unigramTrend = unigramIter.next();
			TopicTrend bigramTrend = bigramIter.next();
			if (!unigramTrend.equals(bigramTrend)) { // their id and query must be equivalent!
				System.out.println("Error: unigram id doesn't match bigram id.");
				System.exit(-1);
			}
			TopicTrend q = new TopicTrend(unigramTrend.id, unigramTrend.query, 
					unigramTrend.unigramCounts, bigramTrend.bigramCounts);
			mergeSet.add(q);
		}
		return mergeSet;
	}
}
