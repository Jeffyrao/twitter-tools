package cc.twittertools.udf;

import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.BagFactory;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

import cc.twittertools.index.LowerCaseEntityPreservingFilter;

public class LuceneBigramTokenizer extends EvalFunc<DataBag>{
	TupleFactory mTupleFactory = TupleFactory.getInstance();
    BagFactory mBagFactory = BagFactory.getInstance();
    
    public DataBag exec(Tuple input) throws IOException{
	    try {
	        DataBag output = mBagFactory.newDefaultBag();
	        Object o = input.get(0);
	        if (!(o instanceof String)) {
	            throw new IOException("Expected input to be chararray, but  got " + o.getClass().getName());
	        }
	        Tokenizer source = new WhitespaceTokenizer(Version.LUCENE_43, new StringReader((String)o));
	        TokenStream tokenstream = new LowerCaseEntityPreservingFilter(source);
	        tokenstream.reset();
	        String bigram, lastToken = "";
	        while (tokenstream.incrementToken()){
	        	String token = tokenstream.getAttribute(CharTermAttribute.class).toString();
	        	if (token.length() > 0 && lastToken.length() > 0) {
	        		bigram = lastToken + " " + token;
	        		output.add(mTupleFactory.newTuple(bigram));
	        	}
	        	if (token.length() > 0) {
	        		lastToken = token;
	        	}
	        }
	        return output;
	    } catch (Exception e) {
	        // error handling goes here
	    	throw new IOException("caught exception",e);
	    }
    }
}
