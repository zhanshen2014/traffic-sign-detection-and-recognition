package dto;

import java.sql.Date;

public class FavoriteDTO {
	private String creator;
	private String trafficID;
	private Date createDate;
	private Date modifyDate;
	private Boolean isActive;

	public FavoriteDTO() {

	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getTrafficID() {
		return trafficID;
	}

	public void setTrafficID(String trafficID) {
		this.trafficID = trafficID;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

}
