package com.ese350.roger.myapplication;

import java.util.*;
	import java.util.Collections;
	//import com.csvreader.CsvReader;
	import java.util.Date;
	import java.io.*;
	
public class predict {
	public static void main(String[] args) {
		
		ArrayList<ArrayList<Integer>> history = new ArrayList<ArrayList<Integer>>();
	}
	
	public static double[] predict(ArrayList<ArrayList<Integer>> history) {
		
		//predicted start of rem cycle 1
		double[] sum = new double[18];	//store 18 sums
		double[] sizeCycle = new double[18];
		for(int k = 0; k < sizeCycle.length; k++) {
			sizeCycle[k] = history.size();
		}
		for (int i = 0; i < history.size(); i++) {
			for(int j = 0; j < history.get(i).size(); j++) {
				if (history.get(i).get(j) != 0) {
					sum[j] += history.get(i).get(j);
				}
				else {
					sizeCycle[j]--;
				}
			}
		}
		double start1 = sum[0] / sizeCycle[0];
		double end1 = sum[1] / sizeCycle[1];
		double start2 = sum[2] / sizeCycle[2];
		double end2 = sum[3] / sizeCycle[3];
		double start3 = sum[4] / sizeCycle[4];
		double end3 = sum[5] / sizeCycle[5];
		double start4 = sum[6] / sizeCycle[6];
		double end4 = sum[7] / sizeCycle[7];
		double start5 = sum[8] / sizeCycle[8];
		double end5 = sum[9] / sizeCycle[9];
		double start6 = sum[10] / sizeCycle[10];
		double end6 = sum[11] / sizeCycle[11];
		double start7 = sum[12] / sizeCycle[12];
		double end7 = sum[13] / sizeCycle[13];
		double start8 = sum[14] / sizeCycle[14];
		double end8 = sum[15] / sizeCycle[15];
		double start9 = sum[16] / sizeCycle[16];
		double end9 = sum[17] / sizeCycle[17];
		
		double[] predicted = new double[18];
		predicted[0] = start1;
		predicted[1] = end1;
		predicted[2] = start2;
		predicted[3] = end2;
		predicted[4] = start3;
		predicted[5] = end3;
		predicted[6] = start4;
		predicted[7] = end4;
		predicted[8] = start5;
		predicted[9] = end5;
		predicted[10] = start6;
		predicted[11] = end6;
		predicted[12] = start7;
		predicted[13] = end7;
		predicted[14] = start8;
		predicted[15] = end8;
		predicted[16] = start9;
		predicted[17] = end9;
		
		return predicted;
		
	}	
		
		
		
		
//		rem1.add(history.get(1).get(1) + history.get(2).get(1) + history.get(3).get(1)
//				+history.get(4).get(1) + history.get(5).get(1) + history.get(6).get(1)
//				+history.get(7).get(1) + history.get(8).get(1) + history.get(9).get(1));
//		rem2 = history.get(2);
//		rem3 = history.get(3);
//		rem4 = history.get(4);
//		rem5 = history.get(5);
//		rem6 = history.get(6);
//		rem7 = history.get(7);
//		rem8 = history.get(8);
//		rem9 = history.get(9);
//		
		
		
		
		
//		history.add(rem1);
//		history.add(rem2);
//		history.add(rem3);
//		history.add(rem4);
//		history.add(rem5);
//		history.add(rem6);
//		history.add(rem7);
//		history.add(rem8);
//		history.add(rem9);
//		history.add(rem10);
}
