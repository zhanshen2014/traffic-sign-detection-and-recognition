package dao;

import java.util.ArrayList;

import json.FavoriteJSON;
import dto.FavoriteDTO;

public interface FavoriteDAO {
	
	public boolean add(FavoriteDTO favorite);

	public boolean delete(FavoriteDTO favorite);

	public ArrayList<FavoriteDTO> listFavorite(String creator,
			Boolean getInActive);

	public ArrayList<FavoriteDTO> listFavorite(String creator);

}
