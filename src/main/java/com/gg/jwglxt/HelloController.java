package com.gg.jwglxt;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javax.script.ScriptException;
import java.io.IOException;

public class HelloController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private Button loginBtn;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button getCoursesBtn;

    public String username;
    public String password;
    public boolean isLogin = false;

    @FXML
    void getCoursesBtnClicked(ActionEvent event){
        try {
            if(isLogin){
                user.getCourses();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,"您还未登录");
                alert.show();
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR,"课程查询失败");
            alert.show();
        }
    }


    public User user = new User();

    @FXML
    void loginBtnClicked(ActionEvent event){

        username = usernameTextField.getText();
        password = passwordField.getText();
        try {
            if(user.Login(username,password)){
                isLogin = true;
                Alert alert = new Alert(Alert.AlertType.INFORMATION,"登陆成功,欢迎\n登陆人："+user.getStuName());
                alert.show();
            }
        }
        catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR,"登陆失败");
            alert.show();
            e.printStackTrace();
        }

    }

}
