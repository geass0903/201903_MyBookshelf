package jp.gr.java_conf.nuranimation.my_bookshelf.model.entity;

public class SearchParam {

    private final String keyword;
    private final int page;

    private SearchParam(String keyword, int page){
        this.keyword = keyword;
        this.page = page;
    }

    public String getKeyword(){
        return keyword;
    }

    public int getPage(){
        return page;
    }

    public static SearchParam setSearchParam(String keyword, int page){
        return new SearchParam(keyword, page);
    }

}
