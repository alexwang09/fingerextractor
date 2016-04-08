package com.wxy.test;

import java.util.ArrayList;

import javax.management.Query;

public class HttpRequest {
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public ArrayList<String> getPageComponentsList() {
		return pageComponentsList;
	}

	public void setPageComponentsList(ArrayList<String> pageComponentsList) {
		this.pageComponentsList = pageComponentsList;
	}

	public ArrayList<String> getKeyList() {
		return keyList;
	}

	public void setKeyList(ArrayList<String> keyList) {
		this.keyList = keyList;
	}

	private String method;
	private String host;
	private ArrayList<String> pageComponentsList = new ArrayList<>();
	private ArrayList<String> keyList = new ArrayList<>();

	public HttpRequest(String tempString) {
		int methodNum = -1, httpReqStartNum = -1, httpReqEndNum = -1, hostEndNum = -1, pcsStartNum = -1, pcsEndNum = -1, keyStartNum = -1, keyEndNum = -1;
		methodNum = tempString.indexOf(".dat");
		if (methodNum >= 0) {
			if (tempString.substring(methodNum + 8, methodNum + 12).equals(
					"POST")) {
				this.method = "POST";
			} else {
				this.method = "GET";
			}
		}

		httpReqStartNum = tempString.indexOf("http://");
		if (httpReqStartNum >= 0) {
			tempString = tempString.substring(httpReqStartNum + 7);
			httpReqEndNum = tempString.indexOf("\"");
			tempString = tempString.substring(0, httpReqEndNum);

			hostEndNum = tempString.indexOf("/");
			if (hostEndNum >= 0) {
				this.host = tempString.substring(0, hostEndNum);
				tempString = tempString.substring(hostEndNum);
				pcsEndNum = tempString.indexOf("?");
				String page = null, query = null;
				if (pcsEndNum >= 0) {
					page = tempString.substring(0, pcsEndNum);

					query = tempString.substring(pcsEndNum + 1);
				//	System.out.println(query);
					while (query.length() > 0) {
						keyEndNum = query.indexOf("=");
						if (keyEndNum > 0) {
							keyList.add(query.substring(0, keyEndNum));

							keyStartNum = query.indexOf("&");
							if (keyStartNum >= 0) {
								query = query.substring(keyStartNum + 1);
								if (query.length() > 0) {
									while (query.substring(0, 1).equals("&")) {
										query = query.substring(1);
									}
								}
							} else
								break;
						}
					}

				//	for (int i = 0; i < keyList.size(); i++) {
				//		System.out.print(keyList.get(i) + "  ");
				//	}
				//	System.out.print("\n");
				} else {
					page = tempString;
				}

				while (page.contains("/") && page.length() > 1) {
					page = page.substring(1);
					pcsStartNum = page.indexOf("/");
					if (pcsStartNum >= 0) {
						if (!page.substring(0, pcsStartNum).contains(".")) {
							pageComponentsList.add(page.substring(0,
									pcsStartNum));
						}
						page = page.substring(pcsStartNum);
					} else {
						if (!page.contains(".")) {
							pageComponentsList.add(page);
						}
					}
				}

			} else {
				this.host = tempString;
			}

		}
	}
}
