register 'jar/elephant-bird-core-4.5.jar';
register 'jar/elephant-bird-pig-4.5.jar';
register 'jar/elephant-bird-hadoop-compat-4.5.jar';
register 'jar/json-simple-1.1.1.jar';
register 'jar/twitter-tools-hadoop-1.0-SNAPSHOT.jar';
register 'jar/twitter-tools-core-1.4.3-SNAPSHOT.jar'; 
register 'jar/lucene-core-4.8.0.jar';
register 'jar/lucene-analyzers-common-4.8.0.jar';
register 'jar/twitter-text-1.9.0.jar';

raw = load '/shared/collections/Tweets2013/' using com.twitter.elephantbird.pig.load.JsonLoader('-nestedLoad');

a = foreach raw generate $0#'created_at',$0#'text';
b = foreach a generate cc.twittertools.udf.GetDate($0) as date, cc.twittertools.udf.GetInterval($0) as interval, flatten(cc.twittertools.udf.LuceneTokenizer($1)) as token;
e = filter b by cc.twittertools.udf.FilterNonASCIIToken(token);
c = group e by ($0,$1,$2);
d = foreach c generate flatten(group),COUNT(e);

store d into 'wordcount-time-2013';
