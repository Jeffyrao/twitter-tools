package umd.timeseries.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TopicTrendSet implements Iterable<TopicTrend>{
	private static int year;
	private List<TopicTrend> queries = new ArrayList<TopicTrend>();
	
	public static int THRESHOLD = 5;
	public static int INTERVAL = 1440; // 5 minutes; 1 day interval = 24*60 = 1440 
	// This number should match EvaluationFeatures.interval

	public TopicTrendSet(int year) {
		this.year = year;
		if (this.year == 2013) {
			THRESHOLD = 15;
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
	
	public static int computeIntervalSize(String day) {
		Date currDate = null, baseDate = null;
		try {
			if (year == 2011) {
				baseDate = new SimpleDateFormat("yyyy-MM-dd Z").parse("2011-01-23 +0000");
			} else {
				baseDate = new SimpleDateFormat("yyyy-MM-dd Z").parse("2013-02-01 +0000");
			}
			currDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy").parse(day);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long diff = currDate.getTime() - baseDate.getTime();
		long minutes = TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS);
		return (int)(minutes / INTERVAL) + 1;
		//return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
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
			q.queryTime = unigramTrend.queryTime;
			q.MIN_ENTROPY = unigramTrend.MIN_ENTROPY;
			q.timespan = unigramTrend.timespan;
			mergeSet.add(q);
		}
		return mergeSet;
	}
}
