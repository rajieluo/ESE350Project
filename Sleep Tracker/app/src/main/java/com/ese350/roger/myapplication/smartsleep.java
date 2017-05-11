package com.ese350.roger.myapplication;
import java.util.*;
	import java.util.Collections;
	//import com.csvreader.CsvReader;
	import java.util.Date;
	/**
	 * Created by shivasuri on 4/12/17.
	 */

public class smartsleep {
	
	    List<Integer> startEnd = new ArrayList<Integer>();  //contains 2 items: start and end time
	    int start;  //start Time
	    int end;    //end Time
	    int freq;   //frequency of pulse
	    Map<Double, ArrayList<Double>> pulseRate = new TreeMap<Double, ArrayList<Double>>();    //key: pulse; values: start and end times
	    Map<Double, ArrayList<Double>> movement = new TreeMap<Double, ArrayList<Double>>();     //key: movement; values: start and end times
	    //CSVReader reader = new CSVReader(new FileReader(
	          //  "datafile2.csv"));

	    //find mean of an arraylist
	    public static double mean (ArrayList<Double> table) {
	        double total = 0;

	        for (int i = 0; i < table.size(); i++) {
	            double currentNum = table.get(i);
	            total += currentNum;
	        }
	        return total / table.size();
	    }


	    //find standard deviation of arraylist
	    public static double sd (ArrayList<Double> table) {
	        // Step 1:
	        double mean = mean(table);
	        double temp = 0;
	        for (int i = 0; i < table.size(); i++) {
	            double val = table.get(i);
	            // Step 2:
	            double squrDiffToMean = Math.pow(val - mean, 2);
	            // Step 3:
	            temp += squrDiffToMean;
	        }
	        // Step 4:
	        double meanOfDiffs = (double) temp / (double) (table.size());
	        // Step 5:
	        return Math.sqrt(meanOfDiffs);
	    }

	    //ArrayList<Float> time, ArrayList<Double> mov
	    public static ArrayList<Integer> getPeak(ArrayList<Double> hr) {
	        int m = 20; //minute index
	        //int x = 20;
	        double mean_nrem = 0;
	        double sum_nrem = 0;
//	        double mean_rem = 0;
//	        double sum_rem = 0;
	        double threshold = 20; //heart rate threshold for standard deviation
	        ArrayList<Double> hr_10 = new ArrayList<Double>();
	        ArrayList<Double> hr_peaks = new ArrayList<Double>();
	        ArrayList<Integer> REM_times = new ArrayList<Integer>();

	        //assume first 20 minutes of sleep are NREM and calculate mean
	        for (int i = 0; i < 20; i++) {
	            sum_nrem += hr.get(i);
	        }
	        mean_nrem = sum_nrem / 20;    //find mean

	        //search for peak
	        while (m < hr.size()) {
	        	//System.out.println(m);
	        	//if REM peak
	            if (hr.get(m) > mean_nrem + sd(hr)) {
	            	//System.out.println("entered if:" + m);
	                //enter REM period check
	                int rem_duration = 10;
	                int startTime = m;
	                int endTime = 0;
	                int temp = m;
	                double sum_rem = 0;
	                double mean_rem = 0;
	                double sum_checkrem = 0;
	                double mean_checkrem = 0;
	                //rem cycle is at least 10 minutes
	                if (m + 10 >= hr.size()) {
                		break;
                	}
	                else {
		                for (int i = m; i < m + 10; i++) {
		                    sum_rem += hr.get(i);
		                }
		                mean_rem = sum_rem / 10;
	                }
	                for (int i = m; i < m + 10; i++) {
	                	hr_10.add(hr.get(i));
	                }
	                
	                while (mean(hr_10) > mean_nrem) {
	                	System.out.println("mean_nrem " + mean_nrem);
	                	System.out.println("mean hr_10 " + mean(hr_10));
//	                	System.out.println(m + " true");
//	                	System.out.println(m);
//	                	System.out.println("hr_10 " + mean(hr_10));
//	                	System.out.println("mean_nrem " + mean_nrem);
	                	hr_10.clear();
	                	if (m + 10 >= hr.size()) {
	                		endTime = hr.size() - 1;	//clip endTime
	                		break;
	                	}
	                	
	                	for (int i = m; i < m + 10; i++) {
		                	hr_10.add(hr.get(i));
		                }
	                	rem_duration++;
	                	m++;
	                	endTime = m;
	                }
	                //System.out.println("rem duration: " + rem_duration);
	                //rem cycle is at least 10 minutes according to research
	                if (rem_duration >= 10 && endTime != 0) {
	                	REM_times.add(startTime);
	                	REM_times.add(endTime);
	                	rem_duration = 10;
	                	m++;
	                }
	                else {
	                	rem_duration = 10;
	                	m++;
	                }
	                
	                //while user is in REM sleep
//	                while(sd(hr_10) > threshold || mean_rem > mean_nrem) {
////	                	System.out.println("while" + m);
////	                	System.out.println("sd:" + sd(hr_10));
////	                	System.out.println("mean rem - mean_nrem - 15:" + (mean_rem - mean_nrem));
//	                	hr_10.clear();
//	                	for (int i = temp; i < temp + 10; i++) {
//		                	hr_10.add(hr.get(i));
//		                }
//	                    //check if rem cycle is over edge case by pooling the next 20 min
//	                	if (temp + 20 >= hr.size()) {
//	                		endTime = hr.size() - 1;	//clip endTime
//	                		break;
//	                	} else {
//	                		sum_checkrem = 0;
//	                		mean_checkrem = 0;
//		                    for (int i = m; i < m + 20; i++) {
//		                        sum_checkrem += hr.get(i);
//		                    }
//		                    mean_checkrem = sum_checkrem / 20;
//		                    //System.out.println(mean_checkrem);
//		                	//System.out.println(mean_checkrem + " vs. " + (mean_rem - sd(hr)));
//		                    //check if rem sleep is over
//		                    if (mean_checkrem <= mean_rem - sd(hr)) {
//		                        //rem cycle is at least 15 minutes
////		                        if (rem_duration >= 15) {
////		                            REM_times.add(startTime);
////		                            REM_times.add(endTime);
////		                            m = endTime + 1;
////		                        }
//		                    	endTime = m;
//		                        break;
//		                    }
//		                    rem_duration++;
//		                    m++;
//		                    endTime = m;
//		                    sum_rem += hr.get(m);
//		                    mean_rem = sum_rem / rem_duration;
//	                	}
//	                }
//	                //rem cycle is at least 15 minutes
//	                if (rem_duration >= 15) {
//	                    REM_times.add(startTime);
//	                    REM_times.add(endTime);
//	                    m = endTime + 1;
//	                    break;
//	                }
//	                else {
//	                    m++; //increment minute index
//	                }
	            }
	            
	            //else increment NREM mean
	            else {
	                sum_nrem += hr.get(m);
	                mean_nrem = sum_nrem / m;
	                m++; //increment minute index
	                //rem_duration = 0;
	            }
	        }
	        return REM_times;
	    }

	    public static void main(String[] args) {
	        Scanner scanIn = null;
	        String InputLine = "";
	        String xfileLocation;
	        xfileLocation = "";

	        ArrayList<Double> samplehr = new ArrayList<Double>();
	        samplehr.add(79.0);
	        samplehr.add(83.32352941);
	        samplehr.add(82.7538461);
	        samplehr.add(83.07575758);
	        samplehr.add(82.9545454);
	        samplehr.add(82.69230769);
	        samplehr.add(82.78125);
	        samplehr.add(79.58928571);
	        samplehr.add(69.61538462);
	        samplehr.add(97.02777778);
	        samplehr.add(91.06349206);
	        samplehr.add(88.578125);
	        samplehr.add(88.53968254);
	        samplehr.add(88.19354839);
	        samplehr.add(88.29032258);
	        samplehr.add(87.78688525);
	        samplehr.add(88.08196721);
	        samplehr.add(87.50847458);
	        samplehr.add(87.59322034);
	        samplehr.add(87.36206897);
	        samplehr.add(87.58333333);
	        samplehr.add(87.5);
	        samplehr.add(87.95081967);
	        samplehr.add(87.75);
	        samplehr.add(87.85);
	        samplehr.add(87.75);
	        samplehr.add(87.55932203);
	        samplehr.add(87.56666667);
	        samplehr.add(87.6440678);
	        samplehr.add(81.59016393);
	        samplehr.add(74.15492958);
	        samplehr.add(73.79710145);
	        samplehr.add(72.83606557);
	        samplehr.add(72.96774194);
	        samplehr.addAll(Arrays.asList(72.83606557, 72.91935484, 72.75, 72.87096774, 72.85245902, 72.85483871, 72.90322581, 72.9516129, 72.88709677, 72.8852459, 72.81967213, 72.86885246, 72.61016949, 72.80327869, 72.6, 72.43103448, 72.63333333, 72.73333333, 72.55932203, 72.45762712, 89.0483871, 124.2033898, 112.0178571, 67.6440678, 66.84210526, 65.88333333, 65.54385965, 66.26984127, 67.15517241, 70.109375, 70.15625, 70.50746269, 69.63333333, 81.74576271, 83.24590164, 83.453125, 83.42857143, 79.76363636, 79.24615385, 79.32307692, 79.15625, 69.5, 65.16393443, 100.8928571, 85.9, 61.90322581, 70.38571429, 92.38983051, 84.56140351, 86.43859649, 84.90163934, 84.05660377, 78.0, 78.12280702, 78.58333333, 78.13793103, 78.56666667, 78.31666667, 78.56666667, 73.10344828, 65.1, 64.96610169, 64.86440678, 65.01666667, 65.1, 65.1147541, 65.08333333, 64.98305085, 64.96666667, 64.89830508, 64.82758621, 64.77586207, 65.05, 64.93220339, 64.98333333, 64.96610169, 65.0, 64.98305085, 65.05, 65.16393443, 65.16393443, 65.16393443, 64.91525424, 65.06666667, 60.28571429, 47.70491803, 47.78333333, 47.83870968, 51.17647059, 69.31666667, 103.1016949, 124.2898551, 119.1929825, 119.8275862, 119.8813559, 66.94444444, 61.6779661, 62.171875, 61.6440678, 61.29824561, 61.51724138, 63.43396226, 65.94117647, 64.70175439, 64.55357143, 64.82142857, 63.1147541, 63.42857143, 72.01639344, 103.9166667, 97.5625, 106.2241379, 105.4067797, 90.25454545, 83.61403509, 83.18181818, 82.88679245, 84.77777778, 74.18644068, 66.14516129, 77.89285714, 79.0, 72.09090909, 58.3125, 56.88059701, 56.28333333, 56.39344262, 56.10344828, 56.23728814, 56.3, 56.39344262, 56.32786885, 56.2, 56.22033898, 56.2, 56.27118644, 43.54545455));
	        samplehr.addAll(Arrays.asList(
	        31.16129032,
	        31.25806452,
	        31.22580645,
	        31.08333333,
	        31.14754098,
	        31.17460317,
	        31.21311475,
	        31.19354839,
	        31.18032787,
	        31.14516129,
	        31.15,
	        31.1147541,
	        31.15873016,
	        31.24590164,
	        31.16393443,
	        31.08333333,
	        31.12903226,
	        31.32258065,
	        31.40298507,
	        117.1969697,
	        115.3174603,
	        116.0454545,
	        113.4745763,
	        111.2539683,
	        101.1639344,
	        89.55555556,
	        89.10169492,
	        80.31746032,
	        80.359375,
	        81.25714286,
	        80.38095238,
	        79.8,
	        101.3432836,
	        105.3770492,
	        105.6721311,
	        101.765625,
	        92.15625,
	        90.60344828,
	        90.88333333,
	        79.14285714,
	        71.2,
	        71.18644068,
	        61.075,
	        58.20338983,
	        58.23728814,
	        58.37704918,
	        58.22033898,
	        58.22033898,
	        58.28813559,
	        58.18644068,
	        58.28813559,
	        58.31666667,
	        58.39344262,
	        58.10169492,
	        58.18644068,
	        61.50909091,
	        71.36065574,
	        71.953125,
	        71.47540984,
	        71.21666667,
	        72.41176471,
	        71.76190476,
	        72.38983051,
	        74.22222222,
	        93.32,
	        112.6944444,
	        107.5272727,
	        111.5652174,
	        100.5517241,
	        86.25490196,
	        77.51388889,
	        78.95588235,
	        78.65151515,
	        77.86885246,
	        77.6440678,
	        63.42307692,
	        56.44067797,
	        56.96923077,
	        56.44067797,
	        56.32758621,
	        125.875,
	        147.2063492,
	        146.6129032,
	        146.1774194,
	        120.119403,
	        55.46875,
	        61.35384615,
	        62.15873016,
	        62.1875,
	        97.75862069,
	        109.862069,
	        107.3076923,
	        94.72881356,
	        94.82758621,
	        95.05,
	        95.39344262,
	        93.24074074,
	        87.73214286,
	        88.0,
	        80.32692308,
	        91.19047619));

	        ArrayList<Double> datafile = new ArrayList<Double>();
	        datafile.addAll(Arrays.asList(
	        		43.0,
	        		43.11428571,
	        		42.96825397,
	        		42.984375,
	        		43.01538462,
	        		43.01538462,
	        		42.77966102,
	        		42.984375,
	        		43.0,
	        		43.22535211,
	        		43.36708861,
	        		43.1,
	        		42.98461538,
	        		43.02941176,
	        		46.03508772,
	        		46.06153846,
	        		46.07575758,
	        		46.40789474,
	        		46.63095238,
	        		46.07246377,
	        		46.14285714,
	        		46.04347826,
	        		46.19444444,
	        		46.10144928,
	        		46.03030303,
	        		46.01515152,
	        		45.90322581,
	        		46.2173913,
	        		46.08695652,
	        		46.49367089,
	        		46.22535211,
	        		46.14492754,
	        		45.93548387,
	        		45.80327869,
	        		45.85245902,
	        		46.41333333,
	        		45.88709677,
	        		45.70491803,
	        		45.75,
	        		45.8,
	        		110.7368421,
	        		151.9830508,
	        		152.7666667,
	        		152.3050847,
	        		152.35,
	        		152.3559322,
	        		151.7288136,
	        		152.7833333,
	        		152.2881356,
	        		152.1694915,
	        		152.5,
	        		153.2131148,
	        		153.1639344,
	        		153.5901639,
	        		152.7666667,
	        		152.9,
	        		153.442623,
	        		153.295082,
	        		154.1428571,
	        		154.0806452,
	        		144.2,
	        		76.27419355,
	        		76.16393443,
	        		76.32786885,
	        		76.29508197,
	        		76.14754098,
	        		76.2,
	        		76.16393443,
	        		76.1,
	        		76.16666667,
	        		76.16666667,
	        		76.47619048,
	        		76.32786885,
	        		76.46031746,
	        		76.37704918,
	        		76.27419355,
	        		76.36065574,
	        		76.46031746,
	        		77.71830986,
	        		76.35483871,
	        		76.24590164,
	        		77.4,
	        		77.50724638,
	        		76.8,
	        		77.30882353,
	        		78.06756757,
	        		77.04477612,
	        		76.98484848,
	        		77.52173913,
	        		77.34782609,
	        		77.38571429,
	        		77.23529412,
	        		77.10606061,
	        		77.02985075,
	        		76.87692308,
	        		77.2238806,
	        		78.62962963,
	        		76.828125,
	        		77.13432836,
	        		77.04545455,
	        		77.34782609,
	        		77.20895522,
	        		77.17647059,
	        		77.39130435,
	        		77.23880597,
	        		77.25,
	        		68.56896552,
	        		64.92537313,
	        		64.97014925,
	        		64.98507463,
	        		64.89552239,
	        		64.6875,
	        		65.04411765,
	        		65.11594203,
	        		65.13043478,
	        		65.14285714,
	        		65.05882353,
	        		65.05970149,
	        		64.81818182,
	        		65.36619718,
	        		111.25,
	        		111.2238806,
	        		111.4264706,
	        		111.3676471,
	        		111.5882353,
	        		112.5,
	        		110.5,
	        		111.1343284,
	        		110.5230769,
	        		110.6923077,
	        		110.8636364,
	        		112.2816901,
	        		110.5538462,
	        		113.9552239,
	        		112.4090909,
	        		112.4848485,
	        		111.3870968,
	        		111.3709677,
	        		111.6349206,
	        		111.8095238,
	        		110.9836066,
	        		111.90625,
	        		114.2083333,
	        		112.4,
	        		114.3888889,
	        		111.4677419,
	        		114.0422535,
	        		111.2419355,
	        		115.4078947,
	        		110.7666667,
	        		71.53333333,
	        		70.71212121,
	        		70.20967742,
	        		70.34920635,
	        		70.30645161,
	        		70.88059701,
	        		70.97058824,
	        		70.79104478,
	        		71.41666667,
	        		69.93333333,
	        		70.609375,
	        		70.4375,
	        		69.39285714,
	        		70.38095238,
	        		71.05797101,
	        		70.75757576,
	        		69.47368421,
	        		69.84745763,
	        		70.56923077,
	        		70.4375,
	        		70.515625,
	        		70.71212121,
	        		70.515625,
	        		70.53125,
	        		68.40983607,
	        		61.23333333,
	        		61.51612903,
	        		61.625,
	        		62.66666667,
	        		63.53125,
	        		63.53846154,
	        		63.515625,
	        		67.85245902,
	        		70.38095238,
	        		70.484375,
	        		70.41269841,
	        		70.328125,
	        		70.484375,
	        		70.74242424,
	        		70.81818182,
	        		70.72727273,
	        		70.61538462,
	        		70.58461538,
	        		70.546875,
	        		70.49230769,
	        		70.34920635,
	        		70.38095238,
	        		70.47692308,
	        		70.3968254,
	        		70.41269841,
	        		70.3968254,
	        		70.33333333,
	        		70.30645161,
	        		70.43076923,
	        		70.4375,
	        		70.25806452,
	        		70.33870968,
	        		70.34920635,
	        		70.56923077,
	        		70.42857143,
	        		70.38095238,
	        		70.453125,
	        		70.46875,
	        		80.73846154,
	        		136.5862069,
	        		138.3064516,
	        		138.3709677,
	        		136.3166667,
	        		67.11666667,
	        		79.22641509,
	        		62.48387097,
	        		62.52380952,
	        		62.58064516,
	        		62.47540984,
	        		62.31147541,
	        		62.3,
	        		55.11111111,
	        		33.98214286,
	        		93.09090909,
	        		97.49206349,
	        		99.265625,
	        		98.70491803,
	        		99.07936508,
	        		97.47368421,
	        		101.1290323,
	        		102.8474576,
	        		102.9833333,
	        		101.1730769,
	        		65.875,
	        		60.54098361,
	        		60.10526316,
	        		86.03333333,
	        		98.57377049,
	        		69.88461538,
	        		77.5862069,
	        		78.375,
	        		78.14285714,
	        		92.27160494,
	        		110.3770492,
	        		110.2903226,
	        		109.95,
	        		109.5932203,
	        		94.14814815,
	        		50.80327869,
	        		50.73333333,
	        		50.74576271,
	        		50.80327869,
	        		50.90322581,
	        		58.0,
	        		61.07936508,
	        		65.91935484,
	        		74.37931034,
	        		74.46666667,
	        		76.80769231,
	        		75.94202899,
	        		74.28813559,
	        		74.52542373,
	        		74.58333333,
	        		74.5,
	        		74.56666667,
	        		74.67213115,
	        		75.72058824,
	        		74.49152542,
	        		74.70491803,
	        		74.17241379,
	        		74.63333333,
	        		74.74193548,
	        		74.72131148,
	        		74.75806452,
	        		74.81967213,
	        		73.3220339,
	        		66.40983607,
	        		66.38709677,
	        		66.37704918,
	        		66.41935484,
	        		66.36065574,
	        		66.29508197,
	        		66.35483871,
	        		66.41935484,
	        		66.41935484,
	        		66.32258065,
	        		66.39344262,
	        		66.29508197,
	        		66.16666667,
	        		66.29508197,
	        		66.29508197,
	        		66.23333333,
	        		66.24590164,
	        		66.3,
	        		66.2295082,
	        		66.4,
	        		66.21666667,
	        		66.32786885,
	        		66.57142857,
	        		78.88,
	        		98.39655172,
	        		98.07017544,
	        		98.07017544,
	        		99.921875,
	        		97.6,
	        		98.61016949,
	        		97.96491228,
	        		97.38888889,
	        		98.55932203,
	        		91.42592593,
	        		57.10714286,
	        		57.01785714,
	        		56.98113208,
	        		73.27272727,
	        		102.1186441,
	        		93.58928571,
	        		75.54545455,
	        		64.62,
	        		58.89285714,
	        		55.32758621,
	        		55.07142857));
	        
	        ArrayList<Integer> final_rem_times = new ArrayList<Integer>();
	        final_rem_times = getPeak(datafile);
	        
	        for (int i = 0; i < final_rem_times.size(); i++) {
	        	System.out.println(final_rem_times.get(i));
	        }
	        //return new merged list if end time is super close to next start time (within 20 minutes)
	    }
}
