package region.jidogam.domain.admin.dto;

public record AdminLoginRequest(
    String email,
    String password
) {

}
