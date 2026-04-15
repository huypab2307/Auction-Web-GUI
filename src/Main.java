class Main{
    public static void main(String[] args) {
        Arts art = FactoryItem.createBuilder(Arts.Builder.class, "monaliza","day la buc tranh nang mozlasdai", 4324, "A001")
                        .withArtist("van goth")
                        .withDimensions("500 x 400")
                        .build();
        System.out.println(art.getArtist());
        Electronics electronic = FactoryItem.createBuilder(Electronics.Builder.class, "Fridge", "Day la cai tu lanh", 6767, "FR01")
                        .withBrand("Samsung")
                        .withColor("blue")
                        .withCurrent(500)
                        .withPower(1000)
                        .withStatus("New")
                        .withVoltage(300)
                        .withWeight(100)
                        .build();
    }
}