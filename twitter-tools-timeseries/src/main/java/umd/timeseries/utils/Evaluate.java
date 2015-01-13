package umd.timeseries.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import umd.twittertools.data.Tweet;
import umd.twittertools.data.TweetSet;
import umd.twittertools.eval.Evaluation;
import umd.twittertools.run.RunTemporalModel;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Table;
import com.google.common.io.Files;

public class Evaluate {
	
	public static Map<Integer, TweetSet> fromFile (String fileAddr) throws IOException {
		List<String> lines = Files.readLines(new File(fileAddr), Charsets.UTF_8);
		Map<Integer, TweetSet> query2TweetSet = new HashMap<Integer, TweetSet>();
		int prevQid = -1;
		int rank = 0;
		for (String line : lines) {
			String[] groups = line.split(" ");
			int label = Integer.parseInt(groups[0]);
			Integer qid = Integer.parseInt(groups[1].split(":")[1]);
			double score = Double.parseDouble(groups[2].split(":")[1]);
			long tweetId = Long.parseLong(groups[5]);
			rank++;
			if (!qid.equals(prevQid)) {
				rank = 0;
				query2TweetSet.put(qid, new TweetSet(qid));
			}
			Tweet tweet = new Tweet(tweetId, rank, 0, 0, score);
			TweetSet tweetSet = query2TweetSet.get(qid);
			tweetSet.add(tweet);
			query2TweetSet.put(qid, tweetSet);
			prevQid = qid;
		}
		
		return query2TweetSet;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Map<Integer, TweetSet> query2TweetSet = fromFile(args[0]);
		Table<Integer, Long, Integer> qrels = RunTemporalModel.loadGroundTruth(args[1]);
		double map = 0, p30 = 0;
		for (int qid: query2TweetSet.keySet()) {
			map += Evaluation.MAP(qid, query2TweetSet.get(qid), qrels, RunTemporalModel.numrels);
			p30 += Evaluation.P_RANK(qid, query2TweetSet.get(qid), qrels, 30);
		}
		int numOfQueries = query2TweetSet.keySet().size();
		System.out.println("MAP:"+map/numOfQueries + " P30:" + p30/numOfQueries);
	}

}
