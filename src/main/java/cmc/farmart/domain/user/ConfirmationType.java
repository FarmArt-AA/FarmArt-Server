package cmc.farmart.domain.user;

public enum ConfirmationType {
    NOT_DEFINED(Boolean.FALSE),
    SERVICE(Boolean.TRUE),
    PRIVACY(Boolean.TRUE),
    PROMOTION(Boolean.FALSE);

    private Boolean required;

    ConfirmationType(Boolean required) {
        this.required = required;
    }

    public Boolean required() {
        return this.required;
    }
}
