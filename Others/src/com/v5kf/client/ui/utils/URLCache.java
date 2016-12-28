package com.v5kf.client.ui.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class URLCache {
	private static Map<String, String> cache = Collections
			.synchronizedMap(new HashMap<String, String>());
	
	public String get(String id) {
		return cache.get(id);
	}
	
	public void put(String id, String value) {
		cache.put(id, value);
	}
	
	public void clear() {
		cache.clear();
	}
}
