package services;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import play.libs.F.Promise;
import services.JobManager.JobContainer;
import jobs.FindMissingFileMatches;
import jobs.FixDuplicateDatabaseImageEntries;
import jobs.FixMissingFiles;
import jobs.ManagedJob;

public class JobManager {
	static List<Class> jobClasses = new ArrayList<Class>(Arrays.asList(
		FixDuplicateDatabaseImageEntries.class,
		FixMissingFiles.class,
		FindMissingFileMatches.class
	));
	
	static JobManager instance = null;
	public static JobManager getInstance() {
		if (instance == null) instance = new JobManager();
		return instance;
	}
	
	List<JobContainer> jobs; 
	public JobManager() {
		jobs = new LinkedList<>();
		int index = 0;
		for (Class c : jobClasses) {
			try {
				jobs.add(new JobContainer(c, index++));
			} catch (Exception e) {
				System.out.println("Failed to load job");
				e.printStackTrace();
			}
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					for (JobContainer job : jobs) {
						job.updateStatus();
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						//ignore
					}
				}
			}
			
		}).start();
	}
	
	public class JobContainer {
		Class job;
		String name;
		String status;
		int index;
		boolean active;
		ManagedJob activeJob;
		Promise activePromise;
		
		public JobContainer(Class job, int index) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
			this.job = job;
			this.name = (String)job.getMethod("getJobName", null).invoke(job.newInstance());
			this.status = "Inactive";
			this.index = index;
			this.active = false;
			this.activeJob = null;
			this.activePromise = null;
		}
		
		public void updateStatus() {
			if (this.active) {
				if (this.status.equals("Running")) {
					if (this.activePromise.isCancelled()) {
						this.status = "Cancelled";
					} else if (this.activePromise.isDone()){
						this.status = "Complete";
						this.active = false;
						System.out.println("Task Completed: "+this.name);
					}
				}
			}
		}

		public void start() {
			try {
				this.activeJob = (ManagedJob)job.newInstance();
				this.activePromise = this.activeJob.now();
				this.active = true;
				this.status = "Running";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void stop() {
			if (this.activePromise.cancel(true)) {
				this.activePromise = null;
				this.activeJob = null;
				this.active = false;
				this.status = "Cancelled";
			}
		}
	}

	public List<JobContainer> getJobs() {
		return jobs;
	}

	public void startJob(int index) {
		JobContainer job = jobs.get(index);
		if (job.active) return;
		
		job.start();
	}
	
	public void stopJob(int index) {
		JobContainer job = jobs.get(index);
		if (!job.active) return;
		job.stop();
	}
}
