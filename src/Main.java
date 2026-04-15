class Main{
    public static void main(String[] args) {
        Arts art = FactoryItem.createBuilder(Arts.Builder.class, "monaliza","day la buc tranh nang mozlasdai", 4324, "A001")
                        .withArtist("van goth")
                        .withDimensions("500 x 400")
                        .build();
        System.out.println(art.getArtist());
    }
}