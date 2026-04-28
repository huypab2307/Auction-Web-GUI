package Items;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import Auction.Auction;
import Database.ArtsDAO;
import Database.AuctionDAO;

public class Arts extends Item {
    private String artist; //Tác giả
    private int yearOfcreation; //Năm sáng tác
    private String dimensions; //Kích thước
    private String medium; //Chất liệu 

    
    public String getArtist(){
        return this.artist;
    }
    public void setArts(String artist, int yearOfcreation, String dimensions, String medium) {
        this.artist = artist;
        this.yearOfcreation = yearOfcreation;
        this.dimensions = dimensions;
        this.medium = medium;
    }

    public Arts(String name, String description, ItemType type,int id, int sellerId, String imagePath) {
        super(name, description, type, sellerId, id, imagePath);
    }
    // YearOfCreation
    public int getYearOfcreation(){
        return this.yearOfcreation;
    }
    
    // Dimensions
    public String getDimensions(){
        return this.dimensions;
    }

    // Medium
    public String getMedium(){
        return this.medium;
    }
    @Override
    public boolean upload(Connection connection, double price, double stepPrice, int durations) throws SQLException {
        int id = ArtsDAO.getInstance().createItem(connection, this);
        return AuctionDAO.getInstance().createAuction(connection, id ,this.getSellerId(), price, stepPrice, durations);
    }
    @Override
    public Map<String, String> getSpecificInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("Tác giả", artist);
        info.put("Năm sáng tác", String.valueOf(yearOfcreation));
        info.put("Kích thước", dimensions);
        info.put("Chất liệu", medium);
        return info;
    }
}