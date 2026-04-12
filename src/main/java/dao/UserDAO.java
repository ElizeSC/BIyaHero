package dao;
import model.User;

public interface UserDAO {
    User login(String username, String password);
    void addUser(User user);
    void updateUser(User user);
    boolean usernameExists(String username);
}