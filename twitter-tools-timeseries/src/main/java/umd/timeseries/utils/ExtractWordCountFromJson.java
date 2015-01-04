package umd.timeseries.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import umd.timeseries.data.TopicTrendSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ExtractWordCountFromJson {
	
	static Gson gson = new Gson();
	
	public static TopicTrendSet read (String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		TopicTrendSet topics = gson.fromJson(br, TopicTrendSet.class);	
		return topics;
	}
	
	public static void main(String[] args) throws IOException {
		TopicTrendSet topics = read("data/unigram_trend.txt");
		System.out.println(topics);
	}
}
