package region.jidogam.domain.user.dto;

public record EmailAuthRequest (
    String email,
    String authCode
){

}
