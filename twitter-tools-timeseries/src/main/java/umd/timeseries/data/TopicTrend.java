package umd.timeseries.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicTrend {
	public int id;
	public String query;
	public String queryTime;
	public int timespan;
	public float MIN_ENTROPY;
	public String maxUnigram;
	public String maxBigram;
	public Map<String, List<Integer>> unigramCounts;
	public Map<String, List<Integer>> bigramCounts;
	public Map<String, Float> unigramEntropy;
	public Map<String, Float> bigramEntropy;
	
	public static final float offset = 1.0f;
	
	public TopicTrend (int id, String query) {
		this.id = id;
		this.query = query;
		this.unigramCounts = new HashMap<String, List<Integer>>();
		this.bigramCounts = new HashMap<String, List<Integer>>();
		this.unigramEntropy = new HashMap<String, Float>();
		this.bigramEntropy = new HashMap<String, Float>();
	}
	
	public TopicTrend (int id, String query, Map<String, List<Integer>> unigramCounts, 
			Map<String, List<Integer>> bigramCounts) {
		this.id = id;
		this.query = query;
		this.unigramCounts = unigramCounts;
		this.bigramCounts = bigramCounts;
		this.unigramEntropy = new HashMap<String, Float>();
		this.bigramEntropy = new HashMap<String, Float>();
	}
	
	public void setQueryTime(String queryTime) {
		this.queryTime = queryTime;
		this.timespan = TopicTrendSet.computeDayDiff(queryTime);
	}
	
	public void setUnigramCounts (Map<String, List<Integer>> unigramCounts) {
		this.unigramCounts = unigramCounts;
	}
	
	public void setBigramCounts (Map<String, List<Integer>> bigramCounts) {
		this.bigramCounts = bigramCounts;
	}
	
	public void addUnigram(String unigram, List<Integer> counts) {
		this.unigramCounts.put(unigram, counts);
	}
	
	public void addBigram(String bigram, List<Integer> counts) {
		this.bigramCounts.put(bigram, counts);
	}
	
	public void cutCounts() {
		for (String unigram: unigramCounts.keySet()) {
			List<Integer> counts = new ArrayList<Integer>(unigramCounts.get(unigram).subList(0, timespan));
			unigramCounts.put(unigram, counts);
		}
		for (String bigram: bigramCounts.keySet()) {
			List<Integer> counts = new ArrayList<Integer>(bigramCounts.get(bigram).subList(0, timespan));
			bigramCounts.put(bigram, counts);
		}
	}
	
	public void computeMinEntropy() {
		MIN_ENTROPY = 0;
		for (int i = 0; i < timespan; i++) {
			float uniformProb = 1.0f / timespan;
			MIN_ENTROPY += uniformProb * Math.log(uniformProb) / Math.log(2);
		}
	}
	
	public void computeEntropy() {
		maxUnigram = computeEntropy(unigramCounts, unigramEntropy);
		maxBigram = computeEntropy(bigramCounts, bigramEntropy);
	}
	
	public List<Integer> getUnigramCount(String unigram) {
		return unigramCounts.get(unigram);
	}
	
	public List<Integer> getBigramCount(String bigram) {
		return bigramCounts.get(bigram);
	}
	
	public String computeEntropy(Map<String, List<Integer>> counts, Map<String, Float> entropies) {
		float maxEntropy = this.MIN_ENTROPY;
		String maxWord = null;
		
		if (counts.size() > 0) {
			for (Map.Entry<String, List<Integer>> entry: counts.entrySet()) {
				int sum = 0; 
				float entropy = 0.0f;
				for (int count: entry.getValue()) {
					sum += count + offset;
				}
				if (sum < TopicTrendSet.THRESHOLD + entry.getValue().size() * offset) {
					entropies.put(entry.getKey(), this.MIN_ENTROPY);
					continue;
				}
				for (int count: entry.getValue()) {
					float prob = (count+offset) * 1.0f / sum;
					if (prob > 0) {
						entropy += prob * Math.log(prob) / Math.log(2);
					}
				}
				entropies.put(entry.getKey(), entropy);
				if (entropy > maxEntropy) {
					maxEntropy = entropy;
					maxWord = entry.getKey();
				}
			}
		}
		
		return maxWord;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) { 
			return true; 
		} 
		if (obj == null || obj.getClass() != this.getClass()) { 
			return false; 
		} 
		
		TopicTrend t = (TopicTrend) obj;
		return this.id == t.id && this.query.equals(t.query);
	}
}
