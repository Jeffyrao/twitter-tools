package cc.twittertools.udf;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

public class GetInterval extends EvalFunc<String>{
	public String exec(Tuple input) throws IOException {
		if(input == null || input.size() == 0){
			return null;
		}
		//Standard Time Format: Tue Feb 08 23:59:59 +0000 2011
		try{
			String str = (String) input.get(0);
			if(str == null) return null;
			String[] groups = str.split("\\s+");
			String time = groups[3];
			String[] timeGroups= time.split(":");
			int interval = (Integer.valueOf(timeGroups[0]))*12 + (Integer.valueOf(timeGroups[1])/5);
			//int interval = Integer.valueOf(timeGroups[0]);
			return String.valueOf(interval);
		}catch(Exception e){
		  throw new IOException("caught exception: " + input.toString() + "isNull:"
          + (input==null) + ", size:" + input.size(), e);
		}
	}
}
