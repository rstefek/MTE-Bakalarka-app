package info.stefkovi.studium.mte_bakalarka.model;

public class LoginResultApiModel {
    public String jwt;
    public UserApiModel user;

    @Override
    public String toString() {
        return "LoginResultApiModel{" +
                "jwt='" + jwt + '\'' +
                ", user=" + user +
                '}';
    }
}
