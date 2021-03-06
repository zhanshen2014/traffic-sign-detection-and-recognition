package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import dao.BaseDAO;
import dao.ReportDAO;
import dto.ReportDTO;

public class ReportDAOImpl implements ReportDAO {
	public boolean add(ReportDTO reportDTO) {
		Connection connection = null;
		PreparedStatement stm = null;
		try {
			connection = BaseDAO.getConnect();
			stm = connection.prepareStatement("INSERT INTO trafficdb.report"
					+ " (content,referenceID, creator, createDate, type,"
					+ "isRead, isActive)" + " VALUES (?, ?,?, NOW(),?, ?, ?)");
			stm.setString(1, reportDTO.getContent());
			stm.setString(2, reportDTO.getReferenceID());
			stm.setString(3, reportDTO.getCreator());
			stm.setInt(4, reportDTO.getType());
			stm.setBoolean(5, false);
			stm.setBoolean(6, true);
			return stm.executeUpdate() > 0;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (stm != null) {
				try {
					stm.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;

	}

	public ArrayList<ReportDTO> searchReportByType(int type) {		
		return this.searchReportByType(type, false);
	}
	
	public ArrayList<ReportDTO> searchReportByType(int type, Boolean getRead) {
		ArrayList<ReportDTO> reportData = new ArrayList<ReportDTO>();
		Connection connection = null;
		PreparedStatement stm = null;
		try {
			connection = BaseDAO.getConnect();
			if (type == 0 && getRead == false) {
				stm = connection
						.prepareStatement("SELECT * FROM trafficdb.report WHERE isRead != ? AND isActive = true ORDER BY reportID DESC");
				stm.setBoolean(1, true);
			} else if (type == 0 && getRead == true) {
				stm = connection
						.prepareStatement("SELECT * FROM trafficdb.report WHERE isActive = true ORDER BY reportID DESC");
			} else if (getRead == false) {
				stm = connection
						.prepareStatement("SELECT * FROM trafficdb.report WHERE type=? AND isRead != ? AND isActive = true ORDER BY reportID DESC");
				stm.setInt(1, type);
				stm.setBoolean(2, true);
			} else {
				stm = connection
						.prepareStatement("SELECT * FROM trafficdb.report WHERE type=? AND isActive = true ORDER BY reportID DESC");
				stm.setInt(1, type);
			}
			ResultSet rs = stm.executeQuery();
			while (rs.next()) {
				ReportDTO reportObj = new ReportDTO();
				reportObj.setReportID(rs.getInt("reportID"));
				reportObj.setReferenceID(rs.getString("referenceID"));
				reportObj.setContent(rs.getString("content"));
				reportObj.setCreator(rs.getString("creator"));
				Timestamp tempTimeStamp = rs.getTimestamp("createDate");		
				Date tempDate = new Date(tempTimeStamp.getTime());
				reportObj.setCreateDate(tempDate);
				reportObj.setType(rs.getInt("type"));
				reportObj.setIsRead(rs.getBoolean("isRead"));
				reportObj.setIsActive(rs.getBoolean("isActive"));
				reportData.add(reportObj);
			}
			return reportData;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stm != null) {
				try {
					stm.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return reportData;
	}

	public ReportDTO getReportDetail(int reportID) {
		Connection connection = null;
		PreparedStatement stm = null;
		try {
			connection = BaseDAO.getConnect();
			stm = connection
					.prepareStatement("SELECT content,referenceID, creator, createDate, type, isRead, isActive FROM trafficdb.report WHERE reportID = ?");
			stm.setInt(1, reportID);
			ResultSet rs = stm.executeQuery();
			if (rs.next()) {
				ReportDTO reportDTO = new ReportDTO();
				reportDTO.setReportID(reportID);
				reportDTO.setContent(rs.getString("content"));
				reportDTO.setReferenceID(rs.getString("referenceID"));
				reportDTO.setCreator(rs.getString("creator"));
				Timestamp tempTimeStamp = rs.getTimestamp("createDate");		
				Date tempDate = new Date(tempTimeStamp.getTime());
				reportDTO.setCreateDate(tempDate);				
				reportDTO.setType(rs.getInt("type"));								
				reportDTO.setIsRead(rs.getBoolean("isRead"));
				reportDTO.setIsActive(rs.getBoolean("isActive"));
				return reportDTO;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (stm != null) {
				try {
					stm.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public boolean delete(int reportID) {
		Connection connection = null;
		PreparedStatement stm = null;
		try {
			connection = BaseDAO.getConnect();
			stm = connection
					.prepareStatement("UPDATE trafficdb.report SET isActive = ? WHERE reportID = ?");
			stm.setBoolean(1, false);
			stm.setInt(2, reportID);
			return stm.executeUpdate() > 0;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (stm != null) {
				try {
					stm.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	public boolean editReport(ReportDTO reportDTO) {
		Connection connection = null;
		PreparedStatement stm = null;
		try {
			connection = BaseDAO.getConnect();
			stm = connection.prepareStatement("UPDATE report SET referenceID = ?, content = ?, creator = ?, type = ?, isRead = ?, isActive = ?  WHERE reportID = ?");
			stm.setString(1, reportDTO.getReferenceID());
			stm.setString(2, reportDTO.getContent());			
			stm.setString(3, reportDTO.getCreator());
			stm.setInt(4, reportDTO.getType());
			stm.setBoolean(5, reportDTO.getIsRead());
			stm.setBoolean(6, reportDTO.getIsActive());
			stm.setInt(7, reportDTO.getReportID());
			return stm.executeUpdate() > 0;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (stm != null) {
				try {
					stm.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
