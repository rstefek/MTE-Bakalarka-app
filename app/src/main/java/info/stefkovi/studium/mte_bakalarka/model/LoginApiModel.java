package info.stefkovi.studium.mte_bakalarka.model;

public class LoginApiModel {
    public String identifier;
    public String password;

    public LoginApiModel(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }
}
