package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Locale;

@SuppressWarnings({"WeakerAccess","unused"})
public class BookData implements Parcelable {

    public static final String JSON_KEY_ITEMS = "Items";
    public static final String JSON_KEY_COUNT = "count";
    public static final String JSON_KEY_LAST = "last";
    public static final String JSON_KEY_ERROR = "error";
    public static final String JSON_KEY_ERROR_DESCRIPTION = "error_description";
    public static final String JSON_KEY_TITLE = "title";
    public static final String JSON_KEY_AUTHOR = "author";
    public static final String JSON_KEY_PUBLISHER_NAME = "publisherName";
    public static final String JSON_KEY_ISBN = "isbn";
    public static final String JSON_KEY_SALES_DATE = "salesDate";
    public static final String JSON_KEY_ITEM_PRICE = "itemPrice";
    public static final String JSON_KEY_ITEM_URL = "itemUrl";
    public static final String JSON_KEY_IMAGE_URL = "largeImageUrl";
    public static final String JSON_KEY_REVIEW_AVERAGE = "reviewAverage";

    public static final String STATUS_UNREGISTERED = "0";
    public static final String STATUS_INTERESTED = "1";
    public static final String STATUS_UNREAD = "2";
    public static final String STATUS_READING = "3";
    public static final String STATUS_ALREADY_READ = "4";
    public static final String STATUS_NONE = "5";

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

    @SuppressWarnings("unused")
    public BookData(int view_type, String ISBN, String title, String image, String author, String publisher, String salesDate, String itemPrice,
             String rakutenUrl, String rating, String readStatus, String tags, String finishReadDate, String registerDate){
        this.view_type = view_type;
        this.ISBN = ISBN;
        this.title = title;
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

    public void setRating(float value){
        this.rating = String.format(Locale.JAPAN,"%.1f",value);
    }

    public String getRating() {
        return rating;
    }

    public float getFloatRating() {
        float value;
        try{
            value = Float.parseFloat(this.rating);
        }  catch (NumberFormatException e){
            value = 0.0f;
        }
        return value;
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
