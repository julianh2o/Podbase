package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jobs.FixDuplicateDatabaseImageEntries;
import jobs.FixMissingFiles;
import jobs.ManagedJob;
import play.mvc.With;
import services.JobManager;
import services.JobManager.JobContainer;

@With(Security.class)
public class JobManagerController extends ParentController {
	public static void index() {
		if (!Security.getUser().isRoot()) forbidden("Must be root");
		List<JobContainer> jobs = JobManager.getInstance().getJobs();
		render(jobs);
	}
	
	public static void startJob(int index) {
		if (!Security.getUser().isRoot()) forbidden("Must be root");
		JobManager.getInstance().startJob(index);
		index();
	}
	
	public static void stopJob(int index) {
		if (!Security.getUser().isRoot()) forbidden("Must be root");
		JobManager.getInstance().stopJob(index);
		index();
	}
}
