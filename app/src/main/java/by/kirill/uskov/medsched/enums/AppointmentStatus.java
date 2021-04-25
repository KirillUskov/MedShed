package by.kirill.uskov.medsched.enums;

public enum AppointmentStatus {
    DO(1),
    CANCEL(3),
    MOVE(2),
    NO(0);

    private int weigh;

    AppointmentStatus(int weigh) {
        this.weigh = weigh;
    }

    public int getWeigh() {
        return weigh;
    }
}
