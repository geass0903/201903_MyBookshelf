package jp.gr.java_conf.nuranimation.my_bookshelf;


class BookData {
    private String isbn;
    private String image;
    private String title;
    private String author;
    private String publisher;
    private String salesDate;
    private String itemPrice;
    private String rakutenUrl;
    private String readStatus;
    private String tags;
    private String finishReadDate;
    private String registerDate;
    private boolean registeredFlag;


    void setIsbn(String isbn){
        this.isbn = isbn;
    }

    String getIsbn(){
        return isbn;
    }

    void setImage(String image){
        this.image = image;
    }

    String getImage(){
        return image;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getTitle() {
        return title;
    }

    void setAuthor(String author) {
        this.author = author;
    }

    String getAuthor() {
        return author;
    }

    void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    String getPublisher() {
        return publisher;
    }

    public String getSalesDate() {
        return salesDate;
    }

    public void setSalesDate(String salesDate) {
        this.salesDate = salesDate;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getRakutenUrl() {
        return rakutenUrl;
    }

    public void setRakutenUrl(String rakutenUrl) {
        this.rakutenUrl = rakutenUrl;
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getFinishReadDate() {
        return finishReadDate;
    }

    public void setFinishReadDate(String finishReadDate) {
        this.finishReadDate = finishReadDate;
    }

    public String getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    public boolean isRegisteredFlag() {
        return registeredFlag;
    }

    public void setRegisteredFlag(boolean registeredFlag) {
        this.registeredFlag = registeredFlag;
    }
}
