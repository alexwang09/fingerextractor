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
			int line = 0;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				HttpRequest hr = new HttpRequest(tempString);
				requestList.add(hr);
			    line++;
			}
			System.out.println(line);
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
		// System.out.println(requestList.size());
		ArrayList<ArrayList<HttpRequest>> getClusterList;
		ArrayList<ArrayList<HttpRequest>> posClusterList;
		ArrayList<HttpRequest> getReqList = new ArrayList<>();
		ArrayList<HttpRequest> posReqList = new ArrayList<>();

		for (int i = 0; i < requestList.size(); i++) {
			if (requestList.get(i).getPageComponentsList().size() == 0
					&& requestList.get(i).getKeyList().size() == 0) {
				requestList.remove(i);
				i--;
			} else if (requestList.get(i).getMethod() != null) {
				if (requestList.get(i).getMethod().equals("GET")) {
					getReqList.add(requestList.get(i));
				}
				if (requestList.get(i).getMethod().equals("POST")) {
					posReqList.add(requestList.get(i));
				}
			}
		}
		getClusterList = clusterAlgor(getReqList);
		System.out.println("-------------------------------------------");
		System.out.println("-------------------------------------------");
		System.out.println("-------------------------------------------");
		posClusterList = clusterAlgor(posReqList);
		// ..

	}

	public static ArrayList<ArrayList<HttpRequest>> clusterAlgor2(
			ArrayList<HttpRequest> requestList) {
		return null;
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
							dh = getDistance2(cluster.get(j), requestList.get(i));
							if (dh <= 0.7) {
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
		// System.out.println(clusterList.size());

		for (int k = 0; k < clusterList.size(); k++) {
			for (int m = 0; m < clusterList.get(k).size(); m++) {
				System.out.print("pcs: ");
				for (int n = 0; n < clusterList.get(k).get(m)
						.getPageComponentsList().size(); n++) {
					System.out.print(clusterList.get(k).get(m)
							.getPageComponentsList().get(n)
							+ " ");
				}
				System.out.print("key: ");
				for (int n = 0; n < clusterList.get(k).get(m).getKeyList()
						.size(); n++) {
					System.out.print(clusterList.get(k).get(m).getKeyList()
							.get(n)
							+ " ");
				}
				System.out.println("\n");
			}
			System.out.println("--------------------------------------------");
		}

		return clusterList;
	}

	public static void mergePTAs() {

	}

	//分别计算pcs和k部分的交集，并集则合起来计算
	public static double getDistance2(HttpRequest req1, HttpRequest req2) {
		double dp = 0.0, dq = 0.0, dh = 0.0, pcsInterNum = 0, pcsUnioNum = 0, kInterNum = 0, kUnioNum = 0;
		int i, j;
		int pcs1Size = req1.getPageComponentsList().size();
		int pcs2Size = req2.getPageComponentsList().size();
		int k1Size = req1.getKeyList().size();
		int k2Size = req2.getKeyList().size();
		int pcsMinSize = 0, pcsMaxSize = 0, kMinSize = 0, kMaxSize = 0;

		pcsMinSize = Math.min(pcs1Size, pcs2Size);
		pcsMaxSize = Math.max(pcs1Size, pcs2Size);
		kMinSize = Math.min(k1Size, k2Size);
		kMaxSize = Math.max(k1Size, k2Size);

		for (i = 0; i < pcsMinSize; i++) {
			if (req1.getPageComponentsList().get(i)
					.equals(req2.getPageComponentsList().get(i))) {
				pcsInterNum++;
			} else {
				pcsUnioNum++;
			}
		}
		pcsUnioNum += pcsMaxSize;

		for (i = 0; i < kMinSize; i++) {
			if (req1.getKeyList().get(i).equals(req2.getKeyList().get(i))) {
				kInterNum++;
			} else {
				kUnioNum++;
			}
		}
		kUnioNum += kMaxSize;

		dh = 1 - (pcsInterNum +kInterNum)/( pcsUnioNum+kUnioNum);

		return dh;

	}
	
	// pcs和k完全分开计算
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

		dh = (dp + dq) / 2;
		if (pcsMaxSize == 0) {
			dh = dq;
		}
		if (kMaxSize == 0) {
			dh = dp;
		}

		return dh;

	}
}
