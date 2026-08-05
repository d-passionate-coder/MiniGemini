package org.example.authservice.signup;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
public class SignupRequest {
     @NonNull
     private String email;

     @NonNull
     private String firstName;

     private  String lastName;

     @NonNull
     private String password;

}
