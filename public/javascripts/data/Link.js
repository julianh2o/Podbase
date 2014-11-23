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
		this.renderImage = new Loader("@{ParentController.renderImage}?image={image}");
		
		
		// ############################################
		// ImageBrowser.java
		// ############################################
		this.index = new Loader("@{ImageBrowser.index}");
		this.fetchProjectPath = new Loader("@{ImageBrowser.fetchProjectPath}?project.id={projectId}&path={path}");
		this.setVisible = new Loader("@{ImageBrowser.setVisible}?project.id={projectId}&image.id={imageId}&visible={visible}");
		this.setMultipleVisible = new Loader("@{ImageBrowser.setMultipleVisible}?project.id={projectId}&ids={ids}&visible={visible}");
		this.imageMetadata = new Loader("@{ImageBrowser.imageMetadata}?path={path}");
		this.resolveFile = new Loader("@{ImageBrowser.resolveFile}?path={path}&mode={mode}&project.id={projectId}&scale={scale}&width={width}&height={height}&brightness={brightness}&contrast={contrast}&histogram={histogram}&slice={slice}");
		this.checkHash = new Loader("@{ImageBrowser.checkHash}?path={path}");
		this.downloadAttributes = new Loader("@{ImageBrowser.downloadAttributes}?project.id={projectId}&path={path}&dataMode={dataMode}");
		this.importAttributes = new Loader("@{ImageBrowser.importAttributes}?project.id={projectId}&path={path}&file={file}&dataMode={dataMode}");
		this.fetchInfo = new Loader("@{ImageBrowser.fetchInfo}?project.id={projectId}&path={path}&dataMode={dataMode}");
		this.updateImageAttribute = new Loader("@{ImageBrowser.updateImageAttribute}?attribute.id={attributeId}&value={value}&dataMode={dataMode}");
		this.deleteImageAttribute = new Loader("@{ImageBrowser.deleteImageAttribute}?attribute.id={attributeId}");
		this.createAttribute = new Loader("@{ImageBrowser.createAttribute}?project.id={projectId}&path={path}&attribute={attribute}&value={value}&dataMode={dataMode}");
		this.pasteAttributes = new Loader("@{ImageBrowser.pasteAttributes}?project.id={projectId}&path={path}&jsonAttributes={jsonAttributes}&overwrite={overwrite}&dataMode={dataMode}");
		this.importFromFile = new Loader("@{ImageBrowser.importFromFile}?project.id={projectId}&path={path}");
		this.findImportables = new Loader("@{ImageBrowser.findImportables}?path={path}");
		this.importDirectory = new Loader("@{ImageBrowser.importDirectory}?project.id={projectId}&path={path}");
		this.upload = new Loader("@{ImageBrowser.upload}?file={file}&path={path}");
		this.createDirectory = new Loader("@{ImageBrowser.createDirectory}?path={path}");
		this.deleteFile = new Loader("@{ImageBrowser.deleteFile}?path={path}");
		
		
		// ############################################
		// PodbaseSecure.java
		// ############################################
		this.authenticate = new Loader("@{PodbaseSecure.authenticate}?String={String}&password={password}&hash={hash}&remember={remember}");
		
		
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
		this.changePassword = new Loader("@{UserController.changePassword}?oldpassword={oldpassword}&newpassword={newpassword}");
		this.createUser = new Loader("@{UserController.createUser}?email={email}");
		this.resendActivation = new Loader("@{UserController.resendActivation}?email={email}");
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
		this.findOrphanedData = new Loader("@{Application.findOrphanedData}");
		this.fixOrphanedData = new Loader("@{Application.fixOrphanedData}");
		
		
		// ############################################
		// SearchController.java
		// ############################################
		this.doSearch = new Loader("@{SearchController.doSearch}?project.id={projectId}&query={query}");
		
		
		// ############################################
		// TemplateController.java
		// ############################################
		this.getTemplates = new Loader("@{TemplateController.getTemplates}?project.id={projectId}");
		this.addTemplate = new Loader("@{TemplateController.addTemplate}?project.id={projectId}&templateName={templateName}");
		this.deleteTemplate = new Loader("@{TemplateController.deleteTemplate}?template.id={templateId}");
		this.duplicateTemplate = new Loader("@{TemplateController.duplicateTemplate}?template.id={templateId}&newName={newName}");
		this.renameTemplate = new Loader("@{TemplateController.renameTemplate}?template.id={templateId}&newName={newName}");
		this.exportTemplate = new Loader("@{TemplateController.exportTemplate}?template.id={templateId}");
		this.importTemplate = new Loader("@{TemplateController.importTemplate}?project.id={projectId}&file={file}");
		this.setFolderTemplate = new Loader("@{TemplateController.setFolderTemplate}?project.id={projectId}&template.id={templateId}&path={path}");
		this.clearFolderTemplate = new Loader("@{TemplateController.clearFolderTemplate}?project.id={projectId}&path={path}");
		this.getTemplateForPath = new Loader("@{TemplateController.getTemplateForPath}?project.id={projectId}&path={path}");
		this.getTemplate = new Loader("@{TemplateController.getTemplate}?template.id={templateId}");
		this.addAttribute = new Loader("@{TemplateController.addAttribute}?template.id={templateId}&name={name}&description={description}");
		this.updateAttribute = new Loader("@{TemplateController.updateAttribute}?attribute.id={attributeId}&name={name}&description={description}&hidden={hidden}");
		this.removeAttribute = new Loader("@{TemplateController.removeAttribute}?attribute.id={attributeId}");
		this.updateAttributeOrder = new Loader("@{TemplateController.updateAttributeOrder}?template.id={templateId}&ids={ids}");
		this.downloadTemplates = new Loader("@{TemplateController.downloadTemplates}");
		
		
		
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
		
		this.getAccessTypes.addTransformer("byType",function(data) {
			return _.reduce(data,function(memo,accessInfo) {
				_.each(accessInfo.type,function(type) {
					if (!memo[type]) memo[type] = [];
					memo[type].push(accessInfo);
				});
				return memo;
			},{});
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

