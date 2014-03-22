package service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import json.FavoriteJSON;
import json.LocateJSON;
import json.ResultInput;
import json.ResultJSON;
import json.ResultShortJSON;
import json.TrafficInfoJSON;
import json.TrafficInfoShortJSON;
import utility.Constants;
import utility.GlobalValue;
import utility.Helper;
import utility.ResultInputCompare;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.multipart.FormDataParam;

import dao.CategoryDAO;
import dao.CategoryDAOImpl;
import dao.FavoriteDAO;
import dao.FavoriteDAOImpl;
import dao.ResultDAO;
import dao.ResultDAOImpl;
import dao.TrafficInfoDAO;
import dao.TrafficInfoDAOImpl;
import dao.TrainImageDAO;
import dao.TrainImageDAOImpl;
import dto.CategoryDTO;
import dto.FavoriteDTO;
import dto.ResultDTO;
import dto.TrafficInfoDTO;

@Path("/Traffic")
public class Traffic {

	/**
	 * List all category in db
	 * 
	 * @return
	 */
	@GET
	@Path("/ListAllCategory")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String listAllCategory() {
		ArrayList<CategoryDTO> cateData = new ArrayList<CategoryDTO>();
		CategoryDAO categoryDAO = new CategoryDAOImpl();
		cateData = categoryDAO.listAllCategory();
		Gson gson = new Gson();
		return gson.toJson(cateData);
	}

	/**
	 * List traffic sign by categoryID
	 * 
	 * @param categoryID
	 * @return
	 */
	@GET
	@Path("/ListByCateID")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String listByCateID(@QueryParam("id") int categoryID) {
		ArrayList<TrafficInfoDTO> trafficData = new ArrayList<TrafficInfoDTO>();
		TrafficInfoDAO trafficDAO = new TrafficInfoDAOImpl();
		trafficData = trafficDAO.searchByCateID(categoryID);
		Gson gson = new Gson();
		return gson.toJson(trafficData);
	}

	/**
	 * Search traffic sign by name and cateID (cateID = null to search in all
	 * cate)
	 * 
	 * @param name
	 * @param cateID
	 * @return
	 */
	@GET
	@Path("/SearchManual")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String searchManual(@QueryParam("name") String name,
			@QueryParam("cateID") Integer cateID) {
		if (cateID == null) {
			// search in all category
			cateID = 0;
		}

		// search
		ArrayList<TrafficInfoDTO> listTrafficInfoDTO = new ArrayList<TrafficInfoDTO>();
		TrafficInfoDAO trafficDAO = new TrafficInfoDAOImpl();
		listTrafficInfoDTO = trafficDAO.searchTraffic(name, cateID);

		// get return info
		CategoryDAO cateDao = new CategoryDAOImpl();
		ArrayList<TrafficInfoShortJSON> listTrafficInfoShortJSON = new ArrayList<TrafficInfoShortJSON>();
		for (TrafficInfoDTO trafficInfo : listTrafficInfoDTO) {
			TrafficInfoShortJSON trafficInfoShortJSON = new TrafficInfoShortJSON();
			trafficInfoShortJSON.setTrafficID(trafficInfo.getTrafficID());
			trafficInfoShortJSON.setName(trafficInfo.getName());
			String imageLink = Constants.MAIN_IMAGE_SUB_LINK
					+ trafficInfo.getImage();
			trafficInfoShortJSON.setImage(imageLink);
			trafficInfoShortJSON.setCategoryID(trafficInfo.getCategoryID());
			trafficInfoShortJSON.setCategoryName(cateDao
					.getCategoryName(trafficInfo.getCategoryID()));
			listTrafficInfoShortJSON.add(trafficInfoShortJSON);
		}
		Gson gson = new Gson();
		return gson.toJson(listTrafficInfoShortJSON);
	}

	/**
	 * View traffic detail
	 * 
	 * @param trafficID
	 * @return
	 */
	@GET
	@Path("/ViewDetail")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String viewDetail(@QueryParam("id") String trafficID) {
		TrafficInfoDTO trafficInfo = new TrafficInfoDTO();
		TrafficInfoDAO trafficDAO = new TrafficInfoDAOImpl();
		trafficInfo = trafficDAO.getDetail(trafficID);

		// get return info
		CategoryDAO cateDao = new CategoryDAOImpl();
		TrafficInfoJSON trafficInfoJSON = new TrafficInfoJSON();
		if (trafficInfo != null) {
			trafficInfoJSON.setTrafficID(trafficInfo.getTrafficID());
			trafficInfoJSON.setName(trafficInfo.getName());
			String imageLink = Constants.MAIN_IMAGE_SUB_LINK
					+ trafficInfo.getImage();
			trafficInfoJSON.setImage(imageLink);
			trafficInfoJSON.setCategoryID(trafficInfo.getCategoryID());
			trafficInfoJSON.setCategoryName(cateDao.getCategoryName(trafficInfo
					.getCategoryID()));
			trafficInfoJSON.setInformation(trafficInfo.getInformation());
			trafficInfoJSON.setPenaltyfee(trafficInfo.getPenaltyfee());
			trafficInfoJSON.setCreator(trafficInfo.getCreator());
			trafficInfoJSON.setModifyDate(trafficInfo.getModifyDate());
			Gson gson = new Gson();
			return gson.toJson(trafficInfoJSON);
		}
		return "";
	}

	/**
	 * Search traffic auto
	 * 
	 * @author everything
	 * @param uploadedInputStream
	 * @param fileDetail
	 * @param userID
	 * @param listLocate
	 * @return
	 */
	@POST
	@Path("/SearchAuto")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String searchAuto(
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("userID") String userID,
			@FormDataParam("listLocate") String listLocate) {

		String fileSaveName = UUID.randomUUID().toString();
		if (userID != null) {
			fileSaveName += "-" + userID;
		}
		fileSaveName += "-"+fileDetail.getFileName();

		String uploadedFileLocation = GlobalValue.getWorkPath()
				+ Constants.UPLOAD_FOLDER + fileSaveName;

		// save file
		Helper.writeToFile(uploadedInputStream, uploadedFileLocation);

		ResultDTO result = new ResultDTO();
		result.setCreator(userID);
		result.setUploadedImage(fileSaveName);
		ResultDAO resultDAO = new ResultDAOImpl();
		int resultID = resultDAO.add(result);
		String imagePath = uploadedFileLocation;
		String appDic = GlobalValue.getWorkPath();
		String tmpresult = "";
		ArrayList<String> listCommand = new ArrayList<String>();
		if (listLocate != null && !listLocate.isEmpty()) {
			listLocate = new String(Base64.encode(listLocate));
			listCommand.add("-d");
			listCommand.add(listLocate);
		}
		// save sample to get train image
		listCommand.add("-s");
		listCommand.add("Out/");
		listCommand.add(imagePath);
		tmpresult = Helper.runTSRT(appDic, listCommand);

		JsonParser parser = new JsonParser();
		JsonObject jo = (JsonObject) parser.parse(tmpresult);
		JsonArray ja = jo.getAsJsonArray("Result");

		ArrayList<ResultInput> listResult = new ArrayList<ResultInput>();
		TrainImageDAO trainImageDao = new TrainImageDAOImpl();
		TrafficInfoDAO trafficInfoDao = new TrafficInfoDAOImpl();

		for (JsonElement item : ja) {
			ResultInput tmpRe = new ResultInput();
			JsonObject jo1 = item.getAsJsonObject();
			String imageName = jo1.get("ID").getAsString();
			if (imageName.length() != 0) {
				String trafficInfoId = "";
				if (imageName.toLowerCase().contains("jpg")) {
					// regconize by find object
					trafficInfoId = trainImageDao.getTrafficInfoID(imageName);
				} else {
					// regconize by SVM
					trafficInfoId = imageName;
				}
				if (trafficInfoId.length() != 0) {
					TrafficInfoDTO trafficInfo = trafficInfoDao
							.getDetail(trafficInfoId);
					tmpRe.setTrafficID(trafficInfo.getTrafficID());
					// String imageLink = Constants.MAIN_IMAGE_SUB_LINK
					// + trafficInfo.getImage();
					// tmpRe.setTrafficImage(imageLink);
					// tmpRe.setTrafficName(trafficInfo.getName());
				}
			}

			// get locate
			JsonObject jo2 = jo1.get("Locate").getAsJsonObject();
			int height = jo2.get("height").getAsInt();
			int width = jo2.get("width").getAsInt();
			int x = jo2.get("x").getAsInt();
			int y = jo2.get("y").getAsInt();
			LocateJSON locate = new LocateJSON();
			locate.setHeight(height);
			locate.setWidth(width);
			locate.setX(x);
			locate.setY(y);
			tmpRe.setLocate(locate);
			listResult.add(tmpRe);
		}

		// short listResult by locate for easy view in result
		Collections.sort(listResult, new ResultInputCompare());

		Gson gson = new Gson();
		String output = gson.toJson(listResult);

		result.setResultID(resultID);
		result.setListTraffic(output);
		result.setIsActive(true);
		resultDAO.edit(result);

		// get return info
		ResultJSON resultJSON = new ResultJSON();
		resultJSON.setResultID(resultID);
		resultJSON.setCreator(result.getCreator());
		for (ResultInput resultInput : listResult) {
			TrafficInfoDAO trafficInfoDAO2 = new TrafficInfoDAOImpl();
			TrafficInfoDTO trafficInfoDTO = trafficInfoDAO2
					.getDetail(resultInput.getTrafficID());
			if (trafficInfoDTO != null) {
				String imageLink = Constants.MAIN_IMAGE_SUB_LINK
						+ trafficInfoDTO.getImage();
				resultInput.setTrafficImage(imageLink);
				resultInput.setTrafficName(trafficInfoDTO.getName());
			}
		}

		resultJSON.setListTraffic(listResult);
		resultJSON.setUploadedImage( Constants.UPLOAD_IMAGE_SUB_LINK +result.getUploadedImage());

		return gson.toJson(resultJSON);
	}

	/**
	 * List history
	 * 
	 * @param creator
	 * @return
	 */
	@GET
	@Path("/ListHistory")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String listHistory(@QueryParam("creator") String creator) {
		ArrayList<ResultDTO> listResultDTO = new ArrayList<ResultDTO>();
		ResultDAO resultDAO = new ResultDAOImpl();
		listResultDTO = resultDAO.getResultByCreator(creator, true);

		// get return info
		ArrayList<ResultShortJSON> listResultShortJSON = new ArrayList<ResultShortJSON>();
		for (ResultDTO resultDTO : listResultDTO) {
			ResultShortJSON resultShortJSON = new ResultShortJSON();
			resultShortJSON.setCreator(resultDTO.getCreator());
			resultShortJSON.setResultID(resultDTO.getResultID());
			resultShortJSON.setCreateDate(resultDTO.getCreateDate());
			listResultShortJSON.add(resultShortJSON);
		}
		Gson gson = new Gson();
		return gson.toJson(listResultShortJSON);
	}

	/**
	 * View history
	 * 
	 * @param resultID
	 * @return
	 */
	@GET
	@Path("/ViewHistory")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String viewHistory(@QueryParam("id") int resultID) {
		ResultDAO resultDAO = new ResultDAOImpl();
		ResultDTO resultDTO = resultDAO.getResultByID(resultID);
		if (resultDTO != null) {
			ResultJSON resultJSON = new ResultJSON();
			resultJSON.setResultID(resultDTO.getResultID());
			resultJSON.setCreator(resultDTO.getCreator());
			Gson gson = new Gson();
			Type t = new TypeToken<ArrayList<ResultInput>>() {
			}.getType();
			ArrayList<ResultInput> listTraffic = new ArrayList<ResultInput>();
			listTraffic = gson.fromJson(resultDTO.getListTraffic(), t);

			for (ResultInput resultInput : listTraffic) {
				TrafficInfoDAO trafficInfoDAO2 = new TrafficInfoDAOImpl();
				TrafficInfoDTO trafficInfoDTO = trafficInfoDAO2
						.getDetail(resultInput.getTrafficID());
				if (trafficInfoDTO != null) {
					String imageLink = Constants.MAIN_IMAGE_SUB_LINK
							+ trafficInfoDTO.getImage();
					resultInput.setTrafficImage(imageLink);
					resultInput.setTrafficName(trafficInfoDTO.getName());
				}
			}
			resultJSON.setListTraffic(listTraffic);
			String uploadedImageLink = Constants.UPLOAD_IMAGE_SUB_LINK
					+ resultDTO.getUploadedImage();
			resultJSON.setUploadedImage(uploadedImageLink);
			resultJSON.setCreateDate(resultDTO.getCreateDate());
			return gson.toJson(resultJSON);
		}
		return "";
	}

	// CHUA IMPLEMENT
	/**
	 * Add traffic sign inforamtion
	 * 
	 * @param trafficID
	 * @param name
	 * @param image
	 * @param categoryID
	 * @param information
	 * @param penaltyfee
	 * @param creator
	 * @return
	 */
	@POST
	@Path("/AddTrafficInfo")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response addTrafficInfo(
			@FormDataParam("trafficID") String trafficID,
			@FormDataParam("name") String name,
			@FormDataParam("image") String image,
			@FormDataParam("categoryID") int categoryID,
			@FormDataParam("information") String information,
			@FormDataParam("penaltyfee") String penaltyfee,
			@FormDataParam("creator") String creator) {
		try {
			TrafficInfoDTO trafficObj = new TrafficInfoDTO();
			trafficObj.setTrafficID(trafficID);
			trafficObj.setName(name);
			trafficObj.setImage(image);
			trafficObj.setCategoryID(categoryID);
			trafficObj.setInformation(information);
			trafficObj.setPenaltyfee(penaltyfee);
			trafficObj.setCreator(creator);

			TrafficInfoDAO trafficDAO = new TrafficInfoDAOImpl();
			Boolean result = trafficDAO.add(trafficObj);
			System.out.println(result);
			if (result.equals(true)) {
				String msg = "Successfull";
				return Response.status(200).entity(msg).build();
			} else {
				return Response.status(200).entity("Unsuccessfull").build();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity("Unsuccessfull").build();
	}
}