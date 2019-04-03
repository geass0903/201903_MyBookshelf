package jp.gr.java_conf.nuranimation.my_bookshelf;


class ShelfRowData {
    private String image;
    private String title;
    private String author;
    private String publisher;


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
}
