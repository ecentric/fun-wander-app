package com.funwander.util;

public class Helper {
	
	final static int MAX_INT_VALUE = 32000;
	
	public static void zk(int n) {
		boolean isAll = false;
		int bestTourWeight = MAX_INT_VALUE;
		int [][] edgeWeight = new int[n][n];
		int [] tour = new int[n];
		
		System.out.println("started");
		
		for (int i = 0; i < n; i++) tour[i] = i;
		do {
			
			int tourWeight = 0;
			for(int i = 0; i < n-1; i++ ) {
				//System.out.print(tour[i]);
				double random = 3.5 * 6.3 * 5.6;
				double random2 = 3.2 * 8.3 * 1.6;
				tourWeight += edgeWeight[tour[i]][tour[i + 1]];
			}
			//System.out.println();
			tourWeight += edgeWeight[tour[n-1]][tour[1]];
			
			if (bestTourWeight > tourWeight) {
				
			}
			
			
			int min = 0;
			for (int i = n-1; i > 0; i--) {
				if (tour[i] > tour[i-1]) {
					min = n;
					int k = tour[i-1];
					int minIndex = 0;
					
					for (int j = i; j < n; j++) {
						if (tour[j] > k && tour[j] < min) {
							min = tour[j];
							minIndex = j;
						}
					}
					
					tour[i-1] = min;
					tour[minIndex] = k;
					
					// sort
					for (int j = i; j < n-1; j++) {
						for (int d = i; d < n - j - 1 + i; d++)
							if (tour[d] > tour[d + 1]) {
								k = tour[d];
								tour[d] = tour[d + 1];
								tour[d + 1] = k;
							}
					}
					break;
				}
			}
					
			if (min == 0) {
				// end
				break;
			}
			
		} while (!isAll);
		System.out.println("finished");
	}
	
	public static void main(String[] args) {
		long startTime = System.nanoTime();
		
		Helper.zk(6);
	    
	    long endTime = System.nanoTime();
	    System.out.println("Total execution time: " + (endTime-startTime) + "nano s");
		
	}
	


}
