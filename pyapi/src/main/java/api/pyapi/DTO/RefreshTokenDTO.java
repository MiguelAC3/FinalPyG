package api.pyapi.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenDTO {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
