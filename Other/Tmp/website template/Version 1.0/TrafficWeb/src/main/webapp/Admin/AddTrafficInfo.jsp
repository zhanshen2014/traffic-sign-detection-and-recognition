<%@page import="java.util.ArrayList"%>
<%@page import="json.TrafficInfoShortJSON"%>
<%@page import="utility.GlobalValue"%>
<%@page import="utility.Constants"%>
<%@page import="json.CategoryJSON"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
	ArrayList<CategoryJSON> listCat = (ArrayList<CategoryJSON>) request.getAttribute("cateList");
%>
<script type="text/javascript">
	$(document).ready(function() {
		$("#add_traffic_form").validate({
			rules : {
				trafficID : {
					required : true,					
				},				
				name : {
					required : true					
				},
				categoryID:{
					required : true
				},
				information: {
					required : true
				},
				mainImage:{
					required :{
				        depends: function(element){
				        	var dataAddNewMainImage = document.getElementById("add-main-image").src;		
				    		if( dataAddNewMainImage !== undefined && dataAddNewMainImage.length > 0)
				    		{
				    			return false;
				    		}
				    		return true;
				        }
					},
					accept: "jpeg|jpg|png|bmp"
				}
			},
			errorClass : "help-inline",
			errorElement : "span",
			highlight : function(element, errorClass, validClass) {
				$(element).parents('.control-group').addClass('error');
			},
			unhighlight : function(element, errorClass, validClass) {
				$(element).parents('.control-group').removeClass('error');
				$(element).parents('.control-group').addClass('success');
			}
		});		
	});


	function addTraffic() {
		var result = $("#add_traffic_form").valid();
		if(result == false)
			return false;
		
		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4) {
				showResult(xhr.responseText);
			}
		}
		var tmpForm = document.getElementById("add_traffic_form");		
		
		var formData = new FormData();
		formData.append("trafficID", tmpForm.trafficID.value);
		formData.append("name", tmpForm.name.value);
		var dataAddNewMainImage = document.getElementById("add-main-image").src;		
		if( dataAddNewMainImage !== undefined && dataAddNewMainImage.length > 0)
		{
			console.log(dataAddNewMainImage)
			var dataBlob = dataURItoBlob(dataAddNewMainImage);
			formData.append('mainImage', dataBlob, 'mainImage.jpg');
		}else
		{
			formData.append('mainImage', tmpForm.mainImage.files[0]);
		}
		
		formData.append("categoryID", tmpForm.categoryID.value);
		formData.append("information", tmpForm.information.value);
		formData.append("penaltyfee", tmpForm.penaltyfee.value);
		formData.append('creator', tmpForm.creator.value);
		xhr.open("POST",'<%=GlobalValue.getServiceAddress()%><%=Constants.TRAFFIC_TRAFFIC_ADD%>');
		xhr.overrideMimeType('text/plain; charset=utf-8');
		xhr.send(formData);
	}

	function showResult(result) {
		console.log(result);
		if (result.trim() != "Success") {
			$.gritter.add({
				title : 'Thông báo',
				text : 'Thêm mới thất bại',
				sticky : false
			});
		} else {
			addTrainImage();
		}
	}
	
	function addTrainImage() {
		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4) {
				showAddTrainImageResult(xhr.responseText);
			}
		}
		var tmpForm = document.getElementById("add_traffic_form");		
		
		var formData = new FormData();
		console.log( tmpForm.trainImage.files.length);		
		for(var i = 0; i < tmpForm.trainImage.files.length; i++) 
		{
			formData.append('trainImage[]', tmpForm.trainImage.files[i]);
		}
		formData.append("trafficID", tmpForm.trafficID.value);
		xhr.open("POST",'<%=GlobalValue.getServiceAddress()%><%=Constants.TRAFFIC_TRAFFIC_TRAIN_IMAGE_ADD%>');
		xhr.overrideMimeType('text/plain; charset=utf-8');
		xhr.send(formData);
	}
	
	function showAddTrainImageResult(result) {
		//console.log(result);		
		if (result.trim() != "Success") {
			
		} else {
			var isNorMalAddNew = $('#isNormalAddNew').val();
			if(isNorMalAddNew == true)
			{
				location.reload();
			}else
			{			
				$.gritter.add({
					title : 'Thông báo',
					text : 'Thêm mới thành công',
					sticky : false
				});		
				$("#AddTrafficModal").modal('hide');
			}
		}
	}
	$(document).ready(function(){		
		//$('input[type=checkbox],input[type=radio],input[type=file]').uniform();		
	});
	
	function dataURItoBlob(dataURI) {
	    var binary = atob(dataURI.split(',')[1]);
	    var array = [];
	    for(var i = 0; i < binary.length; i++) {
	        array.push(binary.charCodeAt(i));
	    }
	    return new Blob([new Uint8Array(array)], {type: 'image/jpeg'});
	}
</script>

<div class="modal-dialog">
	<div class="modal-content">
		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal"
				aria-hidden="true">&times;</button>
			<h4 class="modal-title" id="myModalLabel">Thêm mới biển báo</h4>
		</div>
		<div class="modal-body">
			<div class="trafficDetail">
				<div class="contentImgDetails"></div>
				<form id="add_traffic_form" method="post" class="form-horizontal">
					<input type="hidden" name="creator"
								value="<%=(String) session.getAttribute(Constants.SESSION_USERID)%>" />
					<input type="hidden" name="isNormalAddNew" id="isNormalAddNew" value="true"/>
					<div class="control-group" align="left">
						<label class="control-label">Số hiệu biển báo<span class="required-item">*</span>:</label>
						<div class="controls">
							<input style="width: 300px;" id="required" name="trafficID"  id="trafficID"
								type="text" class="span2" />
						</div>
					</div>
					<div class="control-group" align="left">
						<label class="control-label">Tên biển báo<span class="required-item">*</span>:</label>
						<div class="controls">
							<input style="width: 300px;" name="name" type="text" id="name"
								class="span2" />
						</div>
					</div>
					<div class="control-group" align="left">
						<label class="control-label">Hình ảnh<span class="required-item">*</span>:</label>
						<div class="controls">
						<img id = "add-main-image" class="imageDetails" style="margin: auto; width: 50px; height:50px;" />
							<input style="width: 300px;" name="mainImage" id="mainImage" type="file" 
								class="span2" />
						</div>
					</div>
					<div class="control-group" align="left">
						<label class="control-label">Loại biển báo<span class="required-item">*</span>: </label>
						<div class="controls">
							<select style="width: 300px;" name="categoryID" id="categoryID">
								<%
									for (int i = listCat.size() - 1; i >= 0; i--) {
								%>
								<option class="font-Style"
									value="<%=listCat.get(i).getCategoryID()%>"><%=listCat.get(i).getCategoryName()%></option>
								<%
									}
								%>
							</select>
						</div>
					</div>
					<div class="control-group" align="left">
						<label class="control-label">Thông tin<span class="required-item">*</span>:</label>
						<div class="controls">
							<textarea style="width: 500px; height: 100px;" class="span4"
								name="information" id="information"></textarea>
						</div>
					</div>
					<div class="control-group" align="left">
						<label class="control-label">Mức phạt tham khảo:</label>
						<div class="controls">
						<textarea style="width: 500px; height: 50px;" class="span4"
								name="penaltyfee"></textarea>						
						</div>
					</div>
					<div class="control-group" align="left">
						<label class="control-label">Ảnh nhận dạng:</label>
						<div class="controls">
							<input type="file" name="trainImage" multiple/>
						</div>
					</div>
				</form>				
				
			</div>
		</div>
		<div id="footerViewDetail" class="modal-footer">
		<label style="float: left; font-size: 12px;"><span class="required-item">*</span> Trường bắt buộc</label>
			<button type="button" class="btn btn-default" data-dismiss="modal">Đóng</button>
			<button type="submit" class="btn btn-success"
				onclick="addTraffic(); return false;" value="submit">Lưu</button>
		</div>
	</div>
</div>
