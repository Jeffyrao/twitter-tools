package umd.timeseries.utils;

import java.util.List;

import umd.timeseries.data.TopicTrendSet;
import umd.twittertools.kde.Data;
import umd.twittertools.kde.WeightKDE;
import umontreal.iro.lecuyer.probdist.NormalDist;

public class KDEData
{
	private boolean isValid = false;
	private double[] points;
	private double[] weights;
	private Data data;
	private double[] KDEWeights;
	private double ratioSumOne;
	private double ratioMaxOne;
	private NormalDist kernel = new NormalDist();
	
	public KDEData( List<Integer> counts, boolean isQuery )
	{
		int historyLength = counts.size();
		double sum = 0;
		for( double count : counts )
		{
			sum += count;
		}
		
		isValid = validData(counts);
		if(isValid == true || isQuery == true )
		{
			isValid = true;
			points = new double[historyLength];
			weights = new double[historyLength];
			for( int i = 0; i < historyLength; i++ )
			{
				points[i] = i;
				weights[i] = counts.get( i ) / sum;
			}
			data = new Data( points, weights );
			data.computeStatistics();
			
			KDEWeights = WeightKDE.computeDensity( data, kernel, points );
//			double KDEWeightsMin = Double.MAX_VALUE;
//			for( double w : KDEWeights )
//			{
//				if( w < KDEWeightsMin )
//					KDEWeightsMin = w;
//			}
			// normalize
			double KDEWeightsSum = 0;
			double KDEWeightsMax = 0;
			for( int i = 0; i < KDEWeights.length; i++ )
			{
//				KDEWeights[i] -= KDEWeightsMin;
				KDEWeightsSum += KDEWeights[i];
				if( KDEWeights[i] > KDEWeightsMax )
					KDEWeightsMax = KDEWeights[i];
			}
			ratioSumOne = 1 / KDEWeightsSum;
			for( int i = 0; i < KDEWeights.length; i++ )
			{
				KDEWeights[i] *= ratioSumOne;
			}
			ratioMaxOne = 1 / ( KDEWeightsMax * ratioSumOne );
		}
	}
	
	private boolean validData(List<Integer> counts) {
		double sum = 0, max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
		for (int count: counts) {
			sum += count;
			max = Math.max(max, count);
			min = Math.min(min, count);
		}
		return sum > TopicTrendSet.THRESHOLD && max-min >= 5;
	}
	
	public boolean isValid()
	{
		return isValid;
	}
	
	public double[] getKDEWeights()
	{
		return KDEWeights;
	}
	
	public double[] getLogKDEWeights()
	{
		double[] logKDEWeights = new double[KDEWeights.length];
		for( int i = 0; i < KDEWeights.length; ++i )
		{
			logKDEWeights[i] = Math.log( KDEWeights[i] );
		}
		return logKDEWeights;
	}
	
	public double[] getKDEWeightsMaxOne()
	{
		double[] KDEWeightsMaxOne = new double[KDEWeights.length];
		for( int i = 0; i < KDEWeights.length; ++i )
		{
			KDEWeightsMaxOne[i] = KDEWeights[i] * ratioMaxOne;
		}
		return KDEWeightsMaxOne;
	}
	
	public double getKDEWeight( double point )
	{
		return WeightKDE.computeDensity( data, kernel, point ) * ratioSumOne;
	}
	
	public double getLogKDEWeight( double point )
	{
		return Math.log( getKDEWeight( point ) );
	}
	
	public double getKDEWeightMaxOne( double point )
	{
		return getKDEWeight( point ) * ratioMaxOne;
	}
	
	public static void main( String[] args )
	{
		double[] points = { 1, 2, 3, 4, 5 };
//		double[] weights = { 0.1, 0.2, 0.4, 0.2, 0.1 };
		double[] weights = { 0.5, 1, 2, 1, 0.5 };
		Data unigramData = new Data( points, weights );
		unigramData.computeStatistics();
		NormalDist kernel = new NormalDist();
		double[] estimateValues = WeightKDE.computeDensity( unigramData, kernel, points );
		for( double v : estimateValues )
		{
			System.out.print( v*5 + ", " );
		}
		System.out.println();
		
		double[] points2 = { 1, 2 };
		double[] weights2 = { 0.33, 0.67 };
		unigramData = new Data( points2, weights2 );
		unigramData.computeStatistics();
		estimateValues = WeightKDE.computeDensity( unigramData, kernel, points2 );
		for( double v : estimateValues )
		{
			System.out.print( v*2 + ", " );
		}
	}
}
