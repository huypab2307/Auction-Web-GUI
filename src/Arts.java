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
        public void withArtist(String name){
            this.artist = name;
        }
        public void withyearOfCreation(int year ){
            this.yearOfCreation = year;
        }
        public void withDimensions(String dimensions){
            this.dimensions = dimensions;
        }
        public void medium(String medium){
            this.medium = medium;
        }

        public Item build(){
            return new Arts(this);
        }
    }
}
