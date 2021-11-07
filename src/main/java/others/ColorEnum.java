package others;

public enum ColorEnum {
    RED {
        @Override
        public void paint() {
            System.out.println("red");
        }
    },
    BLUE {
        @Override
        public void paint() {
            System.out.println("blue");
        }
    },
    DEFAULT;

    public void paint() {
        System.out.println("default");
    }

    public static void main(String[] args) {
        DEFAULT.paint();
        RED.paint();
        BLUE.paint();

    }
}
