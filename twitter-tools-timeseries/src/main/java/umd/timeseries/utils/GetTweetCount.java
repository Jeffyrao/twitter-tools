package umd.timeseries.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class GetTweetCount {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new FileReader("data/tweet2013-statistics.txt"));
		String line;
		long sum = 0;
		while((line = br.readLine()) != null) {
			String[] arr = line.split(",");
			int count = Integer.parseInt(arr[1]);
			sum += count;
		}
		System.out.println(sum);
	}

}
