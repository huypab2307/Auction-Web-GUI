abstract class Auction{
    private String username;
    private String password;
    public Auction(String username, String password){
        this.username = username;
        this.password = password;
    }
    public String getUsername(){return username;}
    public String getPassword(){return password;}
}