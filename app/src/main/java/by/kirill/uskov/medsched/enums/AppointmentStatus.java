package by.kirill.uskov.medsched.enums;

public enum AppointmentStatus {
    DO(1, "Завершен"),
    CANCEL(3, "Отменен"),
    MOVE(2, "Перенесен"),
    NO(0, "Предстоит");

    private int weigh;
    private String name;

    AppointmentStatus(int weigh, String name) {
        this.weigh = weigh;
        this.name = name;
    }

    public int getWeigh() {
        return weigh;
    }

    public String getName() {
        return name;
    }


}
