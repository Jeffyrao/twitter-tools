package umd.timeseries.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import umd.timeseries.data.TopicTrend;
import umd.timeseries.data.TopicTrendSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class ExtractWordCountFromJson {
	
	static Gson gson = new Gson();
	
	public static TopicTrendSet read (String path, boolean isBigram, int year) throws IOException {
		TopicTrendSet topicTrendSet = new TopicTrendSet(year);
		JsonReader reader = new JsonReader(new FileReader(path));
		reader.beginObject();
		while(reader.hasNext()) {
			int topicId = Integer.parseInt(reader.nextName());
			String topicStr = null;
			TopicTrend topicTrend = null;
			reader.beginObject();
			while(reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("query")) {
					topicStr = reader.nextString();
					topicTrend = new TopicTrend(topicId, topicStr);
				} else if (name.equals("words")) {
					reader.beginObject();
					while (reader.hasNext()) {
						String n_gram = reader.nextName();
						List<Integer> counts = new ArrayList<Integer>();
						reader.beginArray();
						while(reader.hasNext()) {
							counts.add(reader.nextInt());
						}
						reader.endArray();
						if (!isBigram) {
							topicTrend.addUnigram(n_gram, counts);
						} else {
							topicTrend.addBigram(n_gram, counts);
						}
					}
					reader.endObject();
				} else if (name.equals("querytime")) {
					String querytime = reader.nextString();
					topicTrend.setQueryTime(querytime);
				}
			}
			reader.endObject();
			topicTrend.cutCounts(true);
			topicTrend.computeMinEntropy();
			topicTrendSet.add(topicTrend);
		}
		
		reader.endObject();
		reader.close();
		return topicTrendSet;
	}
	
	public static void main(String[] args) throws IOException {
		TopicTrendSet topics = read("data/unigram_trend.txt", false, 2011);
		System.out.println(topics);
	}
}
