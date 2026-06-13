package api.pyapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginDTO {

    @Positive(message = "Id must be positive")
    private long id;

    @NotBlank(message = "Password is required")
    private String password;
}
