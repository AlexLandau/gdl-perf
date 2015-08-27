package net.alloyggp.perf.runner;

import java.util.List;

import org.ggp.base.util.statemachine.Role;

import com.google.common.collect.ImmutableList;

public class RolesMessage implements GameActionMessage {
    private final ImmutableList<Role> roles;

    private RolesMessage(ImmutableList<Role> roles) {
        this.roles = roles;
    }

    @Override
    public RolesMessage expectRolesMessage() {
        return this;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public static GameActionMessage parse(String line) {
        line = line.substring(GameActionFormat.ROLES_PREFIX.length());

        ImmutableList<Role> roles = GameActionMessage.split(line, Role::new);
        return new RolesMessage(roles);
    }

}
