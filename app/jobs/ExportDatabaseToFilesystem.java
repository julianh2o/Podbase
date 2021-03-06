// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package jobs;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import models.DatabaseImage;
import models.Project;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharSet;
import org.apache.log4j.helpers.FileWatchdog;
import org.yaml.snakeyaml.Yaml;

import controllers.ImageBrowser;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import services.ImportExportService;
import services.PathService;
import util.PodbaseUtil;

@Every("4h")
public class ExportDatabaseToFilesystem extends Job {
	public void doJob() throws Exception {
		Path p = PathService.getRootImageDirectory();
		Files.walkFileTree(p,new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
				if (PathService.isImage(path)) {
					DatabaseImage dbi = DatabaseImage.forPath(path);
					ImportExportService.exportData(dbi);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
