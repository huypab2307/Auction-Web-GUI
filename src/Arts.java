public class Arts extends Item {
    private String artist; //Tác giả
    private String yearOfcreation; //Năm sáng tác
    private String dimensions; //Kích thước
    private String medium; //Chất liệu 
    public Arts (String name, String description, Double price, String artist, String yearOfcreation, String dimensions, String medium) {
        super(name, description, price, "A");
        this.artist = artist;
        this.yearOfcreation = yearOfcreation;
        this.dimensions = dimensions;
        this.medium = medium;
    }
    // Artist
    public String getArtist(){
        return this.artist;
    }

    public void setArtist(String artist){
        this.artist = artist;
    }
    // YearOfCreation
    public String getYearOfcreation(){
        return this.yearOfcreation;
    }
    
    public void setYearOfcreation(String yearOfcreation){
        this.yearOfcreation = yearOfcreation;
    }
    // Dimensions
    public String getDimensions(){
        return this.dimensions;
    }

    public void setDimensions(String dimensions){
        this.dimensions = dimensions;
    }
    // Medium
    public String getMedium(){
        return this.medium;
    }

    public void setMedium(String medium){
        this.medium = medium;
    }

    
}
