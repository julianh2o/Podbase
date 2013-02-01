package services;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import notifiers.Email;

import org.apache.commons.lang.RandomStringUtils;

import controllers.ParentController;

import access.AccessType;

import play.Play;
import play.mvc.Util;

import models.Activation;
import models.DatabaseImage;
import models.Directory;
import models.ImageSetMembership;
import models.Paper;
import models.Permission;
import models.PermissionedModel;
import models.Project;
import models.User;

public class PathPermissionService {
	public static boolean canAccessPath(Project project, Path path) {
		Path root = PathService.getRootImageDirectory();
		try {
			if (path.startsWith(root)) return true;
		} catch (Exception e) {
			return false;
		}
		return false;
	}
}
