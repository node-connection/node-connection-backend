package node.connection.dto.user.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JoinDTO {

    private String username;
    private String password;
    private String phonenum;
}
