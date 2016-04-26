package com.wxy.fingerextractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

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
			// System.out.println(line);
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
		ArrayList<ArrayList<HttpRequest>> getClusterList;
		ArrayList<ArrayList<HttpRequest>> posClusterList;
		ArrayList<HttpRequest> getReqList = new ArrayList<>();
		ArrayList<HttpRequest> posReqList = new ArrayList<>();
		ArrayList<FingerPrint> getFingerPrints = new ArrayList<>();
		ArrayList<FingerPrint> posFingerPrints = new ArrayList<>();
		ArrayList<PrefixTreeAcceptors> getPTAList = new ArrayList<>();
		ArrayList<PrefixTreeAcceptors> posPTAList = new ArrayList<>();
		int i, j, k, m, p, q;

		for (i = 0; i < requestList.size(); i++) {
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
		for (i = 0; i < getClusterList.size(); i++) {
			FingerPrint fingerPrint = generFingerPrint(getClusterList.get(i));
			getFingerPrints.add(fingerPrint);
		}
		posClusterList = clusterAlgor(posReqList);
		for (i = 0; i < posClusterList.size(); i++) {
			FingerPrint fingerPrint = generFingerPrint(posClusterList.get(i));
			posFingerPrints.add(fingerPrint);
		}
		
		getPTAList=mergeFingerPrints(getFingerPrints);
		posPTAList=mergeFingerPrints(getFingerPrints);
	
		System.out.println(getPTAList.size());
		for (i = 0; i < getPTAList.size(); i++) {
	
			System.out.print("finger" + (i + 1) + ": ");
			Iterator it = getPTAList.get(i).getHostSet().iterator();
			while (it.hasNext()) {
				String fruit = (String) it.next();
				System.out.print(fruit + " ");
			}
			for (j = 0; j < getPTAList.get(i).getStateMachineList().size(); j++) {
				System.out.print(getPTAList.get(i).getStateMachineList().get(j) + " ");
			}
			System.out.print("\n");
		}
	}

	private static int generSimilarity(String host1, String host2) {
		String[] domains1 = host1.split("\\.");
		String[] domains2 = host2.split("\\.");
		int dom1Length = domains1.length;
		int dom2Length = domains2.length;
		int similarity = 0;
		int minLength = Math.min(dom1Length, dom2Length);
		for (int i = 0; i < minLength; i++) {
			if (domains1[dom1Length - 1].equals(domains2[dom2Length - 1])) {
				similarity++;
			}
			dom1Length--;
			dom2Length--;
		}
		if (similarity == domains1.length && similarity == domains2.length) {
			similarity = 10;
		}
		return similarity;
	}

	@SuppressWarnings("null")
	public static FingerPrint generFingerPrint(
			ArrayList<HttpRequest> requestList) {

		FingerPrint fingerPrint = new FingerPrint();
		TreeSet<String> hostSet = new TreeSet<>();
		ArrayList<String> stateMachine = new ArrayList<>();
		ArrayList<String> pcStrings = new ArrayList<>();
		ArrayList<String> kStrings = new ArrayList<>();

		int pcsSize = requestList.get(0).getPageComponentsList().size();
		int kSize = requestList.get(0).getKeyList().size();
		int i, j, k, flag = -1;
		// 得到host
		for (i = 0; i < requestList.size(); i++) {
			if (requestList.get(i).getHost() != null) {
				hostSet.add(requestList.get(i).getHost());
			}
		}

		// 得到method
		if (requestList.get(0).getMethod().equals("GET")) {
			stateMachine.add("GET");
		} else {
			stateMachine.add("POST");
		}
		// 得到pcs
		for (i = 0; i < pcsSize; i++) {
			pcStrings.add(requestList.get(0).getPageComponentsList().get(i));
		}

		for (i = 0; i < pcStrings.size(); i++) {
			for (j = 1; j < requestList.size(); j++) {
				flag = -1;
				for (k = 0; k < requestList.get(j).getPageComponentsList()
						.size(); k++) {
					if (pcStrings.get(i).equals(
							requestList.get(j).getPageComponentsList().get(k))) {
						flag = 0;
						break;
					}
				}

				if (flag < 0) {
					pcStrings.remove(i);
					i--;
					break;
				}
			}
		}
		// 得到key
		for (i = 0; i < kSize; i++) {
			kStrings.add(requestList.get(0).getKeyList().get(i));
		}

		for (i = 0; i < kStrings.size(); i++) {
			for (j = 1; j < requestList.size(); j++) {
				flag = -1;
				for (k = 0; k < requestList.get(j).getKeyList().size(); k++) {
					if (kStrings.get(i).equals(
							requestList.get(j).getKeyList().get(k))) {
						flag = 0;
						break;
					}
				}

				if (flag < 0) {
					kStrings.remove(i);
					i--;
					break;
				}
			}
		}

		stateMachine.addAll(pcStrings);
		stateMachine.addAll(kStrings);
		fingerPrint.setHostSet(hostSet);
		fingerPrint.setStateMachine(stateMachine);

		return fingerPrint;
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
							dh = getDistance3(cluster.get(j),
									requestList.get(i));
							if (dh <= 0.6) {
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

		/*
		 * for (int k = 0; k < clusterList.size(); k++) { for (int m = 0; m <
		 * clusterList.get(k).size(); m++) {
		 * System.out.print(clusterList.get(k).get(m).getMethod()+" ");
		 * System.out.print("pcs: "); for (int n = 0; n <
		 * clusterList.get(k).get(m) .getPageComponentsList().size(); n++) {
		 * System.out.print(clusterList.get(k).get(m)
		 * .getPageComponentsList().get(n) + " "); } System.out.print("key: ");
		 * for (int n = 0; n < clusterList.get(k).get(m).getKeyList() .size();
		 * n++) { System.out.print(clusterList.get(k).get(m).getKeyList()
		 * .get(n) + " "); } System.out.println("\n"); }
		 * System.out.println("--------------------------------------------"); }
		 */

		return clusterList;
	}

	public static ArrayList<PrefixTreeAcceptors> mergeFingerPrints(ArrayList<FingerPrint> fingerPrints) {
		ArrayList<PrefixTreeAcceptors> ptaList = new ArrayList<>();
		int i,j,k,m,p,q;
		for (i = 0; i < fingerPrints.size(); i++) {
			PrefixTreeAcceptors pta = new PrefixTreeAcceptors();
			ArrayList<ArrayList<String>> stateMachineList = new ArrayList<>();

			pta.setHostSet(fingerPrints.get(i).getHostSet());
			stateMachineList.add(fingerPrints.get(i).getStateMachine());
			pta.setStateMachineList(stateMachineList);
			ptaList.add(pta);
		}

		for (i = 0; i < ptaList.size(); i++) {
			ArrayList<ArrayList<String>> stateMachineList = new ArrayList<>();
			for (j = 0; j < ptaList.size(); j++) {
				if (i != j) {
					int flag = -1;
					Iterator it1 = ptaList.get(i).getHostSet().iterator();
					for (k = 0; k < ptaList.get(i).getHostSet().size(); k++) {
						String host1 = (String) it1.next();
						Iterator it2 = ptaList.get(j).getHostSet().iterator();
						for (m = 0; m < ptaList.get(j).getHostSet().size(); m++) {
							String host2 = (String) it2.next();
							int hostSimilarity = generSimilarity(host1, host2);
							if (hostSimilarity == 10) {
								ptaList.get(i).getHostSet()
										.addAll(ptaList.get(j).getHostSet());
								ptaList.get(i)
										.getStateMachineList()
										.addAll(ptaList.get(j)
												.getStateMachineList());
								flag = 0;
								break;
							} else if (hostSimilarity >= 2) {
								for (p = 0; p < ptaList.get(i)
										.getStateMachineList().size(); p++) {
									for (q = 0; q < ptaList.get(j)
											.getStateMachineList().size(); q++) {
										if (ptaList.get(i)
												.getStateMachineList().get(p)
												.size() > 2
												&& ptaList.get(j)
														.getStateMachineList()
														.get(q).size() > 2) {
											if (ptaList.get(i)
													.getStateMachineList()
													.get(p).get(1) == ptaList
													.get(j)
													.getStateMachineList()
													.get(q).get(1)) {
												ptaList.get(i)
														.getHostSet()
														.addAll(ptaList.get(j)
																.getHostSet());
												ptaList.get(i)
														.getStateMachineList()
														.addAll(ptaList
																.get(j)
																.getStateMachineList());
												flag = 0;
												break;
											}
										}
									}
								}
							}
						}
						if (flag == 0) {
							ptaList.remove(j);
							j--;
							break;
						}
					}
				}
			}
		}
		return ptaList;
	}

	// 分别计算pcs和k部分的交集，并集则合起来计算;顺序比较
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

		dh = 1 - (pcsInterNum + kInterNum) / (pcsUnioNum + kUnioNum);

		return dh;

	}

	// 无序比较；pcs与k结合
	public static double getDistance3(HttpRequest req1, HttpRequest req2) {
		double dh = 0.0, reqInterNum = 0, reqUnioNum = 0;
		int i, j;
		int pcs1Size = req1.getPageComponentsList().size();
		int pcs2Size = req2.getPageComponentsList().size();
		int k1Size = req1.getKeyList().size();
		int k2Size = req2.getKeyList().size();

		for (i = 0; i < pcs1Size; i++) {
			for (j = 0; j < pcs2Size; j++) {
				if (req1.getPageComponentsList().get(i)
						.equals(req2.getPageComponentsList().get(j))) {
					reqInterNum++;
					break;
				}
			}
		}

		for (i = 0; i < k1Size; i++) {
			for (j = 0; j < k2Size; j++) {
				if (req1.getKeyList().get(i).equals(req2.getKeyList().get(j))) {
					reqInterNum++;
					break;
				}
			}
		}

		reqUnioNum = pcs1Size + pcs2Size + k1Size + k2Size - reqInterNum;

		dh = 1 - reqInterNum / reqUnioNum;

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
