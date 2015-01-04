package umd.timeseries.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicTrend {
	public int id;
	public String query;
	public Map<String, List<Integer>> unigramCounts;
	public Map<String, List<Integer>> bigramCounts;
	
	public TopicTrend (int id, String query) {
		this.id = id;
		this.query = query;
		this.unigramCounts = new HashMap<String, List<Integer>>();
		this.bigramCounts = new HashMap<String, List<Integer>>();
	}
}
