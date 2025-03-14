package naegamaja_server.naegamaja.domain.game.model;

import lombok.Getter;

@Getter
public enum TeamColor {
    BLACK("black"),
    WHITE("white"),
    RED("red");

    private final String value;

    TeamColor(String value) {
        this.value = value;
    }

}
