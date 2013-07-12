// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package util;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import access.AccessType;

import services.ImportExportService;
import services.PathService;
import services.PermissionService;

import models.DatabaseImage;
import models.Project;
import models.ProjectVisibleImage;
import models.User;

public class Stopwatch {
	HashMap<String,Record> records;
	public Stopwatch() {
		records = new HashMap<String,Record>();
	}
	
	public void clear() {
		records.clear();
	}
	
	public void start(String name) {
		if (records.containsKey(name)) {
			Record rec = records.get(name);
			rec.start();
		} else {
			records.put(name, new Record());
		}
	}
	
	public void stop(String name) {
		if (!records.containsKey(name)) throw new RuntimeException("invalid stopwatch key! "+name);
		records.get(name).stop();
	}
	
	public double getAverageTime(String name) {
		if (!records.containsKey(name)) throw new RuntimeException("invalid stopwatch key! "+name);
		Record r = records.get(name);
		double averageTime = (double)r.totalTime / (double)r.totalEnds;
		return averageTime;
	}
	
	public String getAverageTimeS(String name) {
		return String.format("%2f", getAverageTime(name));
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Entry<String,Record> entry : records.entrySet()) {
			Record r = entry.getValue();
			double averageTime = (double)r.totalTime / (double)r.totalEnds;
			sb.append(entry.getKey() + ": " + averageTime +"ms\n");
		}
		return sb.toString();
	}
	
	private class Record {
		long totalTime;
		int totalEnds;
		
		long lastStart;
		
		public Record() {
			totalTime = 0;
			totalEnds = 0;
			start();
		}
		
		public void start() {
			lastStart = System.currentTimeMillis();
		}
		
		public void stop() {
			long delta = System.currentTimeMillis() - lastStart;
			totalEnds++;
			totalTime += delta;
		}
	}
}