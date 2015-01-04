package umd.timeseries.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TopicTrendSet implements Iterable<TopicTrend>{
	  
	private List<TopicTrend> queries = new ArrayList<TopicTrend>();

	public TopicTrendSet() {}
	
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
}
