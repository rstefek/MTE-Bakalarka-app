package info.stefkovi.studium.mte_bakalarka.model;

public class UserApiModel {
    public int id;
    public String username;
    public String email;

    @Override
    public String toString() {
        return "UserApiModel{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
