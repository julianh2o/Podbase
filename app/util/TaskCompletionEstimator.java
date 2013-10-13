// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package util;

import java.io.File;
import java.nio.file.Path;
import java.text.DecimalFormat;
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

public class TaskCompletionEstimator {
	int resolution;
	int window;
	int expectedTicks;
	
	int currentTick;
	
	LinkedList<Long> slidingWindow;
	
	long lastTick = -1;
	long startTime = -1;
	
	public TaskCompletionEstimator(int window, int resolution, int expectedTicks) {
		this.window = window;
		this.resolution = resolution;
		this.expectedTicks = expectedTicks;
		this.slidingWindow = new LinkedList<Long>();
		this.currentTick = 0;
	}
	
	public void tick() {
		if (startTime == -1) startTime = System.currentTimeMillis();
		if (currentTick % resolution == 0) recordTick();
		currentTick++;
	}
	
	private void recordTick() {
		long current = System.currentTimeMillis();
		if (lastTick != -1) {
			long elapsed = current - lastTick;
			slidingWindow.add(elapsed);
			if (slidingWindow.size() > window) slidingWindow.poll();
		}
		
		lastTick = current;
	}
	
	//Returns the average tick time in milliseconds
	public double getAverageTick() {
		if (slidingWindow.size() == 0) return 0;
		
		long sum = 0;
		for (Long l : slidingWindow) {
			sum += l;
		}
		return (sum / (double)slidingWindow.size()) / (double)resolution;
	}
	
	//Returns the estimated remaining time in milliseconds
	public long getEstimate() {
		double averageTick = getAverageTick();
		
		int remainingTicks = this.expectedTicks - currentTick;
		
		return (long)(averageTick * remainingTicks);
	}
	
	//formats HH:MM:SS estimated completion time
	public String getFormattedEstimate() {
		return formatInterval(getEstimate());
	}
	
	public String formatInterval(long millis) {
		long s = millis/1000;
		return String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
	}

	public String getStatusLine() {
		String perTick;
		if (getAverageTick() > 2000) {
			perTick = new DecimalFormat("#.#####").format(getAverageTick()/1000.0);
			perTick = String.format("%s s/tick",perTick);
		} else {
			perTick = new DecimalFormat("#.#####").format(getAverageTick());
			perTick = String.format("%s ms/tick",perTick);
		}
		long elapsed = System.currentTimeMillis() - startTime;
		return String.format("%s elapsed - [%d/%d] %s  (eta %s)",formatInterval(elapsed),getCurrentTick(),getExpectedTicks(),perTick,getFormattedEstimate());
	}
	
	public int getCurrentTick() {
		return currentTick;
	}

	public int getResolution() {
		return resolution;
	}

	public int getWindow() {
		return window;
	}

	public int getExpectedTicks() {
		return expectedTicks;
	}

	public LinkedList<Long> getSlidingWindow() {
		return slidingWindow;
	}

	public long getLastTick() {
		return lastTick;
	}
}