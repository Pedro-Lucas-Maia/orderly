package bti.pds.dinner.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Role {
    private RoleId id;
    private String name;

    public Role(String name) {
        this.id = new RoleId();
        this.name = name;
    }
}

