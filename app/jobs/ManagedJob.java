// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package jobs;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import models.DatabaseImage;
import models.Project;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharSet;
import org.yaml.snakeyaml.Yaml;

import play.Play;
import play.cache.Cache;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.modules.search.Search;
import play.modules.search.store.FilesystemStore;
import services.PathService;

public abstract class ManagedJob extends Job {
	public String getJobName() {
		return this.getClass().getSimpleName();
	}
}
