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
    private String rating;
    private String readStatus;
    private String tags;
    private String finishReadDate;
    private String registerDate;


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

    String getSalesDate() {
        return salesDate;
    }

    void setSalesDate(String salesDate) {
        this.salesDate = salesDate;
    }

    String getItemPrice() {
        return itemPrice;
    }

    void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    String getRakutenUrl() {
        return rakutenUrl;
    }

    void setRakutenUrl(String rakutenUrl) {
        this.rakutenUrl = rakutenUrl;
    }

    String getReadStatus() {
        return readStatus;
    }

    void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    String getTags() {
        return tags;
    }

    void setTags(String tags) {
        this.tags = tags;
    }

    String getFinishReadDate() {
        return finishReadDate;
    }

    void setFinishReadDate(String finishReadDate) {
        this.finishReadDate = finishReadDate;
    }

    String getRegisterDate() {
        return registerDate;
    }

    void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    String getRating() {
        return rating;
    }

    void setRating(String rating) {
        this.rating = rating;
    }
}
