package umd.timeseries.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FeatureSplit {
	
	public static void featureSplit(String dir, String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(dir + fileName));
		BufferedWriter bw1 = new BufferedWriter(new FileWriter(dir + "train_"+ fileName));
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(dir + "test_" + fileName));
		String line;
		while((line = br.readLine()) != null) {
			String qidStr = line.split(" ")[1];
			int qid = Integer.parseInt(qidStr.split(":")[1]);
			if (qid % 2 == 0) {
				bw1.write(line + "\n");
			} else {
				bw2.write(line + "\n");
			}
		}
		br.close();
		bw1.close();
		bw2.close();
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String path = "features/";
		File folder = new File(path);
		if(folder.isDirectory()){
			for (File file : folder.listFiles()) {
				if(!file.getName().startsWith("features"))
					continue;
				featureSplit(path, file.getName());
			}
		}
	}

}
