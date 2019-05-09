package jp.gr.java_conf.nuranimation.my_bookshelf;


import android.os.Parcel;
import android.os.Parcelable;

class BookData implements Parcelable {
    private int view_type;
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


    BookData(){
    }

    @SuppressWarnings("unused")
    BookData(int view_type, String isbn, String image, String author, String publisher, String salesDate, String itemPrice,
             String rakutenUrl, String rating, String readStatus, String tags, String finishReadDate, String registerDate){
        this.view_type = view_type;
        this.isbn = isbn;
        this.image = image;
        this.author = author;
        this.publisher = publisher;
        this.salesDate = salesDate;
        this.itemPrice = itemPrice;
        this.rakutenUrl = rakutenUrl;
        this.rating = rating;
        this.readStatus = readStatus;
        this.tags = tags;
        this.finishReadDate = finishReadDate;
        this.registerDate = registerDate;
    }


    void setView_type(int type){
        this.view_type = type;
    }

    int getView_type(){
        return view_type;
    }

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


    protected BookData(Parcel in) {
        view_type = in.readInt();
        isbn = in.readString();
        image = in.readString();
        title = in.readString();
        author = in.readString();
        publisher = in.readString();
        salesDate = in.readString();
        itemPrice = in.readString();
        rakutenUrl = in.readString();
        rating = in.readString();
        readStatus = in.readString();
        tags = in.readString();
        finishReadDate = in.readString();
        registerDate = in.readString();
    }

    public static final Creator<BookData> CREATOR = new Creator<BookData>() {
        @Override
        public BookData createFromParcel(Parcel in) {
            return new BookData(in);
        }

        @Override
        public BookData[] newArray(int size) {
            return new BookData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(view_type);
        dest.writeString(isbn);
        dest.writeString(image);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(publisher);
        dest.writeString(salesDate);
        dest.writeString(itemPrice);
        dest.writeString(rakutenUrl);
        dest.writeString(rating);
        dest.writeString(readStatus);
        dest.writeString(tags);
        dest.writeString(finishReadDate);
        dest.writeString(registerDate);
    }
}
