public class Arts extends Item {
    private String artist; //Tác giả
    private int yearOfcreation; //Năm sáng tác
    private String dimensions; //Kích thước
    private String medium; //Chất liệu 

    public Arts(Builder build){
        super(build.name, build.description, build.price,"A", build.seller_id);
        this.artist = build.artist;
        this.yearOfcreation = build.yearOfCreation;
        this.dimensions = build.dimensions;
        this.medium = build.medium;
    }
    // Artist
    public String getArtist(){
        return this.artist;
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
    static class Builder{
        private String name;
        private String description;
        private double price;
        private String seller_id;
        private String artist; //Tác giả
        private int yearOfCreation; //Năm sáng tác
        private String dimensions; //Kích thước
        private String medium; //Chất liệu 
        public Builder(String name, String description, double price, String seller_id){
            this.name = name;
            this.description = description;
            this.price = price;
            this.seller_id = seller_id;
        }
        public Builder withArtist(String name){
            this.artist = name;
            return this;
        }
        public Builder withyearOfCreation(int year ){
            this.yearOfCreation = year;
            return this;
        }
        public Builder withDimensions(String dimensions){
            this.dimensions = dimensions;
            return this;
        }
        public Builder medium(String medium){
            this.medium = medium;
            return this;
        }

        public Arts build(){
            return new Arts(this);

        }
    }
}
