package jp.gr.java_conf.nuranimation.my_bookshelf.model.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class BookData implements Parcelable {

    public static final int TYPE_EMPTY             = 0;
    public static final int TYPE_BOOK              = 1;
    public static final int TYPE_BUTTON_LOAD       = 2;
    public static final int TYPE_VIEW_LOADING      = 3;
    
    public static final String STATUS_INTERESTED    = "1";
    public static final String STATUS_UNREAD        = "2";
    public static final String STATUS_READING       = "3";
    public static final String STATUS_ALREADY_READ  = "4";
    public static final String STATUS_NONE          = "5";

    private int view_type;
    private String ISBN;
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


    public BookData(){
    }

    public BookData(@Nullable BookData book){
        if(book != null) {
            this.view_type = book.getView_type();
            this.ISBN = book.getISBN();
            this.title = book.getTitle();
            this.image = book.getImage();
            this.author = book.getAuthor();
            this.publisher = book.getPublisher();
            this.salesDate = book.getSalesDate();
            this.itemPrice = book.getItemPrice();
            this.rakutenUrl = book.getRakutenUrl();
            this.rating = book.getRating();
            this.readStatus = book.getReadStatus();
            this.tags = book.getTags();
            this.finishReadDate = book.getFinishReadDate();
            this.registerDate = book.getRegisterDate();
        }
    }

    public void setView_type(int type){
        this.view_type = type;
    }

    public int getView_type(){
        return view_type;
    }

    public void setISBN(String ISBN){
        this.ISBN = ISBN;
    }

    public String getISBN(){
        return ISBN;
    }

    public void setImage(String image){
        this.image = image;
    }

    public String getImage(){
        return image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setSalesDate(String salesDate) {
        this.salesDate = salesDate;
    }

    public String getSalesDate() {
        return salesDate;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setRakutenUrl(String rakutenUrl) {
        this.rakutenUrl = rakutenUrl;
    }

    public String getRakutenUrl() {
        return rakutenUrl;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }

    public void setFinishReadDate(String finishReadDate) {
        this.finishReadDate = finishReadDate;
    }

    public String getFinishReadDate() {
        return finishReadDate;
    }

    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    public String getRegisterDate() {
        return registerDate;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getRating() {
        return rating;
    }


    protected BookData(Parcel in) {
        view_type = in.readInt();
        ISBN = in.readString();
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
        dest.writeString(ISBN);
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
