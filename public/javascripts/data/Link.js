/* GENERATED SOURCE, DO NOT EDIT */


define(['data/Loader'],function(Loader) {
	var This = function() {
		// ############################################
		// PaperController.java
		// ############################################
		this.render = new Loader("@{PaperController.render}?imagesetid={imagesetid}&size={size}");
		this.getPapers = new Loader("@{PaperController.getPapers}");
		this.getPaper = new Loader("@{PaperController.getPaper}?paper.id={paperId}");
		this.createPaper = new Loader("@{PaperController.createPaper}?name={name}");
		this.deletePaper = new Loader("@{PaperController.deletePaper}?paper.id={paperId}");
		this.getImageSet = new Loader("@{PaperController.getImageSet}?imageset.id={imagesetId}");
		this.addImageToSet = new Loader("@{PaperController.addImageToSet}?imageset.id={imagesetId}&path={path}");
		this.removeImageFromSet = new Loader("@{PaperController.removeImageFromSet}?imageset.id={imagesetId}&path={path}");
		
		
		// ############################################
		// Security.java
		// ############################################
		this.checkPermission = new Loader("@{Security.checkPermission}");
		
		
		// ############################################
		// ParentController.java
		// ############################################
		this.debug = new Loader("@{ParentController.debug}?objects={objects}");
		
		
		// ############################################
		// ImageBrowser.java
		// ############################################
		this.index = new Loader("@{ImageBrowser.index}");
		this.fetchProjectPath = new Loader("@{ImageBrowser.fetchProjectPath}?project.id={projectId}&path={path}");
		this.fetchPath = new Loader("@{ImageBrowser.fetchPath}?path={path}");
		this.resolveFile = new Loader("@{ImageBrowser.resolveFile}?path={path}&mode={mode}&project.id={projectId}&scale={scale}&width={width}&height={height}&brightness={brightness}&contrast={contrast}&histogram={histogram}");
		this.fetchInfo = new Loader("@{ImageBrowser.fetchInfo}?project.id={projectId}&path={path}&dataMode={dataMode}");
		this.updateImageAttribute = new Loader("@{ImageBrowser.updateImageAttribute}?attribute.id={attributeId}&value={value}");
		this.deleteImageAttribute = new Loader("@{ImageBrowser.deleteImageAttribute}?attribute.id={attributeId}");
		this.createAttribute = new Loader("@{ImageBrowser.createAttribute}?project.id={projectId}&path={path}&attribute={attribute}&value={value}&dataMode={dataMode}");
		
		
		// ############################################
		// PermissionController.java
		// ############################################
		this.setPermission = new Loader("@{PermissionController.setPermission}?model.id={modelId}&user.id={userId}&permission={permission}&value={value}");
		this.setUserPermission = new Loader("@{PermissionController.setUserPermission}?user.id={userId}&permission={permission}&value={value}");
		this.getListedUsers = new Loader("@{PermissionController.getListedUsers}?model.id={modelId}");
		this.getCurrentUserPermissions = new Loader("@{PermissionController.getCurrentUserPermissions}");
		this.getCurrentPodbase = new Loader("@{PermissionController.getCurrentPodbase}");
		this.getUserAccess = new Loader("@{PermissionController.getUserAccess}?model.id={modelId}&user.id={userId}");
		this.getAccess = new Loader("@{PermissionController.getAccess}?model.id={modelId}");
		this.addUserByEmail = new Loader("@{PermissionController.addUserByEmail}?model.id={modelId}&email={email}");
		this.removeUser = new Loader("@{PermissionController.removeUser}?model.id={modelId}&user.id={userId}");
		this.getAccessTypes = new Loader("@{PermissionController.getAccessTypes}");
		
		
		// ############################################
		// UserController.java
		// ############################################
		this.getAllUsers = new Loader("@{UserController.getAllUsers}");
		this.createUser = new Loader("@{UserController.createUser}?email={email}");
		this.doActivate = new Loader("@{UserController.doActivate}?activationCode={activationCode}");
		this.completeActivation = new Loader("@{UserController.completeActivation}?user.id={userId}&activationCode={activationCode}&password={password}&confirm={confirm}");
		this.mimicUser = new Loader("@{UserController.mimicUser}?user.id={userId}");
		
		
		// ############################################
		// ProjectController.java
		// ############################################
		this.getProjects = new Loader("@{ProjectController.getProjects}");
		this.getProject = new Loader("@{ProjectController.getProject}?project.id={projectId}");
		this.createProject = new Loader("@{ProjectController.createProject}?name={name}");
		this.setDataMode = new Loader("@{ProjectController.setDataMode}?project.id={projectId}&dataMode={dataMode}");
		this.deleteProject = new Loader("@{ProjectController.deleteProject}?project.id={projectId}");
		this.addDirectory = new Loader("@{ProjectController.addDirectory}?project.id={projectId}&path={path}");
		this.removeDirectory = new Loader("@{ProjectController.removeDirectory}?directory.id={directoryId}");
		
		
		// ############################################
		// ImageViewer.java
		// ############################################
		this.index = new Loader("@{ImageViewer.index}?path={path}");
		this.script = new Loader("@{ImageViewer.script}");
		
		
		// ############################################
		// Application.java
		// ############################################
		this.index = new Loader("@{Application.index}");
		this.entry = new Loader("@{Application.entry}?projectId={projectId}");
		this.paper = new Loader("@{Application.paper}?paperId={paperId}");
		this.loadJavascript = new Loader("@{Application.loadJavascript}?path={path}");
		this.getCurrentUser = new Loader("@{Application.getCurrentUser}");
		
		
		// ############################################
		// TemplateController.java
		// ############################################
		this.getTemplates = new Loader("@{TemplateController.getTemplates}?project.id={projectId}");
		this.addTemplate = new Loader("@{TemplateController.addTemplate}?project.id={projectId}&templateName={templateName}");
		this.setFolderTemplate = new Loader("@{TemplateController.setFolderTemplate}?project.id={projectId}&template.id={templateId}&path={path}");
		this.getTemplateForPath = new Loader("@{TemplateController.getTemplateForPath}?project.id={projectId}&path={path}");
		this.getTemplate = new Loader("@{TemplateController.getTemplate}?template.id={templateId}");
		this.addAttribute = new Loader("@{TemplateController.addAttribute}?template.id={templateId}&name={name}");
		this.updateAttribute = new Loader("@{TemplateController.updateAttribute}?attribute.id={attributeId}&name={name}");
		this.removeAttribute = new Loader("@{TemplateController.removeAttribute}?attribute.id={attributeId}");
		this.updateAttributeOrder = new Loader("@{TemplateController.updateAttributeOrder}?template.id={templateId}&ids={ids}");
		
		
		
		this.getProjects.addTransformer("byId", function(data) {
			var map = {};
			_.each(data, function(item) {
				map[item.id] = item;
			});
			return map;
		});
		
		this.fetchInfo.addTransformer("byAttribute", function(data) {
			return _.groupBy(data,"attribute");
		});
	};
	
	$.extend(This.prototype,{
		loadAll : function(info,callback,repeat) {
			// data = ["str", [a,{params}] ]
			var self = this;
			var called = false;
			
			function getLink(x) {
				if (typeof x == "string") {
					return self[x]();
				} else if (x.length > 1){
					return self[x[0]](x[1]);
				}
				return null;
			}
			
			function handler() {
				var allReady = true;
				_.each(info,function(x){
					var link = getLink(x);
					if (!link.isDataReady()) {
						allReady = false;
						return false;
					}
				});
				if (allReady && (!called || repeat)) {
					var links = _.map(info,function(x) {
						return getLink(x);
					});
					called = true;
					callback.apply(this,links);
				}
			}
			
			_.each(info,function(x) {
				var link = getLink(x);
				link.asap(handler);
			});
		}
	});
	
	var instance = new This();
	window.link = instance;
	return instance;
	
});

