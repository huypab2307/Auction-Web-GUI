class Bidder extends Auction {
    String role;
    public Bidder(String username,String password, String role){
        super(username,password);
        this.role = role;
    }
}
