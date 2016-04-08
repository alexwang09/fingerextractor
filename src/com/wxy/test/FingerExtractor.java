package com.wxy.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FingerExtractor {
	public static void readFileByLines(String fileName) {
		File file = new File(fileName);
		BufferedReader reader = null;
		ArrayList<HttpRequest> requestList = new ArrayList<HttpRequest>();
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				HttpRequest hr = new HttpRequest(tempString);
				requestList.add(hr);
				// System.out.println("line " + line + ": " + tempString);
				// line++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		generPTAs(requestList);
	}

	public static void generPTAs(ArrayList<HttpRequest> requestList) {
		ArrayList<ArrayList<HttpRequest>> clusterList;
		for (int i = 0; i < requestList.size(); i++) {
			if (requestList.get(i).getPageComponentsList().size() == 0
					&& requestList.get(i).getKeyList().size() == 0) {
				requestList.remove(i);
				i--;
			}
		}
		clusterList = clusterAlgor(requestList);
        //..
	}

	public static ArrayList<ArrayList<HttpRequest>> clusterAlgor(
			ArrayList<HttpRequest> requestList) {
		ArrayList<ArrayList<HttpRequest>> clusterList = new ArrayList<>();
		double dh = 0.0;
		int i, j;
		while (requestList.size() > 0) {
			int clusterSize = 0;
			ArrayList<HttpRequest> cluster = new ArrayList<>();
			ArrayList<Integer> removeNum = new ArrayList<>();
			cluster.add(requestList.get(0));
			requestList.remove(0);
			if (requestList.size() <= 0) {
				clusterList.add(cluster);
				break;
			} else {
				while (clusterSize != cluster.size()) {
					removeNum.clear();
					int clusterSize2 = cluster.size();
					for (i = 0; i < requestList.size(); i++) {
						for (j = clusterSize; j < clusterSize2; j++) {
							dh = getDistance(cluster.get(j), requestList.get(i));
							if (dh < 0.6) {
								cluster.add(requestList.get(i));
								requestList.remove(i);
								i--;
								break;
							}
						}
					}
					clusterSize = clusterSize2;

				}
				clusterList.add(cluster);
			}
		}
		System.out.println(clusterList.size());
		/*
		 * for(int k=0;k<clusterList.size();k++){ for(int
		 * m=0;m<clusterList.get(k).size();m++){ for(int
		 * n=0;n<clusterList.get(k).get(m).getPageComponentsList().size();n++){
		 * System
		 * .out.print(clusterList.get(k).get(m).getPageComponentsList().get
		 * (n)+" "); } for(int
		 * n=0;n<clusterList.get(k).get(m).getKeyList().size();n++){
		 * System.out.print(clusterList.get(k).get(m).getKeyList().get(n)+" ");
		 * } System.out.println("\n"); }
		 * System.out.println("--------------------------------------------"); }
		 */
		return clusterList;
	}

	public static void mergePTAs() {

	}

	public static double getDistance(HttpRequest Req1, HttpRequest Req2) {
		double dp = 0.0, dq = 0.0, dh = 0.0, pcsInterNum = 0, pcsUnioNum = 0, kInterNum = 0, kUnioNum = 0;
		int i, j;
		int pcs1Size = Req1.getPageComponentsList().size();
		int pcs2Size = Req2.getPageComponentsList().size();
		int k1Size = Req1.getKeyList().size();
		int k2Size = Req2.getKeyList().size();
		int pcsMinSize = 0, pcsMaxSize = 0, kMinSize = 0, kMaxSize = 0;

		pcsMinSize = Math.min(pcs1Size, pcs2Size);
		pcsMaxSize = Math.max(pcs1Size, pcs2Size);
		kMinSize = Math.min(k1Size, k2Size);
		kMaxSize = Math.max(k1Size, k2Size);

		for (i = 0; i < pcsMinSize; i++) {
			if (Req1.getPageComponentsList().get(i)
					.equals(Req2.getPageComponentsList().get(i))) {
				pcsInterNum++;
			} else {
				pcsUnioNum++;
			}
		}
		pcsUnioNum += pcsMaxSize;

		for (i = 0; i < kMinSize; i++) {
			if (Req1.getKeyList().get(i).equals(Req2.getKeyList().get(i))) {
				kInterNum++;
			} else {
				kUnioNum++;
			}
		}
		kUnioNum += kMaxSize;

		dp = 1 - pcsInterNum / pcsUnioNum;
		dq = 1 - kInterNum / kUnioNum;

		if (pcsMaxSize == 0) {
			dp = 0;
		}
		if (kMaxSize == 0) {
			dq = 0;
		}
		dh = (dp + dq) / 2;

		return dh;

	}
}
