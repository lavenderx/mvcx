package guda.mvcx.demo.test.form;

import io.quasar.demo.form.UserForm;

/**
 * Created by well on 2017/3/20.
 */
public class FormTest {

    public static void main(String[] args){
        UserForm userForm = new UserForm();
        userForm.validateError();
        System.out.println(userForm.getErrorResult().get("userName"));
    }
}
