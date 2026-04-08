public abstract class Person {
    private String username;
    private String password;
    private static int id = 25020000;

    public Person(String username, String password) {
        this.username = username;
        this.password = password;
        this.id = Person.id++;
    }
}
