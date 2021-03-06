package dto;

import java.sql.Date;

public class ResultDTO {
	private int resultID;
	private String uploadedImage;
	private String listTraffic;
	private String creator;
	private Date createDate;
	private Boolean isActive;

	public ResultDTO() {
		// TODO Auto-generated constructor stub
	}

	public int getResultID() {
		return resultID;
	}

	public void setResultID(int resultID) {
		this.resultID = resultID;
	}

	public String getUploadedImage() {
		return uploadedImage;
	}

	public void setUploadedImage(String uploadedImage) {
		this.uploadedImage = uploadedImage;
	}

	public String getListTraffic() {
		return listTraffic;
	}

	public void setListTraffic(String listTraffic) {
		this.listTraffic = listTraffic;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
}
