package User;
public abstract class Person {
    private String username;
    private String password;
    private static int cur_id = 0000;
    private String id;

    public Person(String username, String password) {
        this.username = username;
        this.password = password;
        this.id = "U" +  Person.cur_id++;
    }
}
