class Main{
    public static void main(String[] args) {
        Arts art = FactoryItem.createBuilder(Arts.Builder.class, "monaliza","day la buc tranh nang mozlasdai", 4324, "A001")
                        .withArtist("van goth")
                        .withDimensions("500 x 400")
                        .build();
        System.out.println(art.getArtist());

        Vehicle vehicle = FactoryItem.createBuilder(Vehicle.Builder.class, "SH","This is an SH", 1000, "V001")
                        .withMileage(8463)
                        .withModel("SH")
                        .withTitleStatus("Old")
                        .withTrim("Limited")
                        .build();
        System.out.println(vehicle.getTitleStatus());
    }
}