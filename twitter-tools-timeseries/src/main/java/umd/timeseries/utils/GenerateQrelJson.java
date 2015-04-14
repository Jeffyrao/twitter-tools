package umd.timeseries.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import umd.twittertools.data.Tweet;
import umd.twittertools.data.TweetSet;
import umd.twittertools.run.RunTemporalModel;

import com.google.common.collect.Table;
import com.google.gson.stream.JsonWriter;

public class GenerateQrelJson {
	
	public static Map<Integer, String> loadQuery(String queryFile) throws IOException {
		BufferedReader bf = new BufferedReader(new FileReader(queryFile));
		Map<Integer, String> queryMap = new HashMap<Integer, String>();
		String line, queryStr = null, queryTime = null;
		int queryId = 0;
		while((line = bf.readLine()) != null) {
			if (line.startsWith("<num>")) {
				int beginIndex = line.indexOf("MB");
				int endIndex = line.indexOf(" </num>");
				queryId = Integer.parseInt(line.substring(beginIndex+2, endIndex));
			}
			if (line.startsWith("<title>") || line.startsWith("<query>")) {
			  int beginIndex = 0, endIndex = 0;
			  if (line.startsWith("<title>")) {
			    beginIndex = line.indexOf("<title> ");
			    endIndex = line.indexOf(" </title>");
			  } else {
			    beginIndex = line.indexOf("<query> ");
			    endIndex = line.indexOf(" </query>");
			  }
				queryStr = line.substring(beginIndex+8, endIndex).toLowerCase();
				queryMap.put(queryId, queryStr);
			} else if (line.startsWith("<querytime>")) {
				;
			}
		}
		return queryMap;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String queryFile = "data/topics.microblog2013-2014.txt";
		String searchResultFile = "data/ql.2013.total.txt";
		String qrelsFile = "data/qrels.2013.total.txt";
		Map<Integer, TweetSet> query2TweetSet = TweetSet.fromFile(searchResultFile);
		Table<Integer, Long, Integer> qrels = RunTemporalModel.loadGroundTruth(qrelsFile);
		Map<Integer, String> queryMap = loadQuery(queryFile);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("data/trec2013-2014-data.js"));
		JsonWriter jsonWriter = new JsonWriter(bw);
		jsonWriter.setIndent("\t");
		jsonWriter.beginObject();
		List<Integer> queryIds = new ArrayList<Integer>(query2TweetSet.keySet());
		Collections.sort(queryIds);
		for (int queryId: queryIds) {
			jsonWriter.name(String.valueOf(queryId)).beginObject();
			jsonWriter.name("query").value(queryMap.get(queryId));
			jsonWriter.name("qrels").beginArray();
			for(Tweet tweet: query2TweetSet.get(queryId)){
				jsonWriter.beginArray();
				jsonWriter.value(String.valueOf(tweet.getId()));
				jsonWriter.value(tweet.getTimeDiff());
				int flag = qrels.contains(queryId, tweet.getId()) ? qrels.get(queryId, tweet.getId()) : 0;
				jsonWriter.value(flag);
				jsonWriter.endArray();
			}
			jsonWriter.endArray();
			jsonWriter.endObject();
		}
		jsonWriter.endObject();
		jsonWriter.flush();
		jsonWriter.close();
		bw.close();
	}

}
