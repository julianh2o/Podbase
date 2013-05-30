define(
	['view/RenderedView', 'util/Util', 'data/Link', 'text!tmpl/UserPanel.html'],
	function (RenderedView, Util, Link, tmpl) {
		
		var This = RenderedView.extend({
			template: _.template( tmpl ),
			
			initialize: function() {
				Link.getCurrentUser().asap($.proxy(this.refresh,this));
			},
			
			refresh : function() {
				var user = Link.getCurrentUser().getData();
				this.model = {user:user};
				
				this.render();
				
				$(".show-change-password",this.el).click($.proxy(this.onChangePasswordClicked,this));
				
				this.$changePasswordDialog = $('.change-password-dialog',this.el).modal({show:false});
				
				$(".close-dialog",this.el).click(function() {
					$(this).closest(".modal").modal("hide");
				});
				
				this.$changePasswordDialog.find(".change-password").click($.proxy(this.changePasswordConfirmed,this));
			},
			
			onChangePasswordClicked : function(e) {
				e.preventDefault();
				this.$changePasswordDialog.find("input").val(""); //clear form
				
				this.$changePasswordDialog.modal("show");
			},
			
			changePasswordConfirmed : function() {
				var map = Util.arrayToMap(this.$changePasswordDialog.find("form").serializeArray(),"name","value");
				
				var $group = this.$changePasswordDialog.find("[name=confirm]").closest(".control-group");
				if (map['new'] != map['confirm']) {
					$group.addClass("error");
					$group.find(".help-inline").text("Passwords don't match!");
					return;
				} else {
					$group.removeClass("error");
					$group.find(".help-inline").text("");
				}
				
				Link.changePassword(map['current'],map['new']).post().done($.proxy(this.changePasswordSuccess,this)).error($.proxy(this.changePasswordError,this));
			},
			
			changePasswordSuccess : function(xhr) {
				this.$changePasswordDialog.modal("hide");
			},
			
			changePasswordError : function(xhr) {
				console.log(xhr.status);
				data = JSON.parse(xhr.responseText);
				alert(data.message);
			}
		});
		
		$.extend(This,{
		});
		
		return This;
	}
)