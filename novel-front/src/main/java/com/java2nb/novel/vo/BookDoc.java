package com.java2nb.novel.vo;

import com.java2nb.novel.entity.Book;
import lombok.Data;

import java.util.Date;

@Data
public class BookDoc {
    private Long id;
    private String bookName;
    private String authorName;
    private Integer catId;
    private String catName;
    private Long lastIndexId;
    private String lastIndexName;
    private Integer wordCount;
    private Date updateTime;
    private Long visitCount;
    private Byte bookStatus;
    private String bookDesc;

    public BookDoc(Book book) {
        this.id = book.getId();
        this.bookName = book.getBookName();
        this.authorName = book.getAuthorName();
        this.catId = book.getCatId();
        this.catName = book.getCatName();
        this.lastIndexId = book.getLastIndexId();
        this.lastIndexName = book.getLastIndexName();
        this.wordCount = book.getWordCount();
        this.updateTime = book.getUpdateTime();
        this.visitCount = book.getVisitCount();
        this.bookStatus = book.getBookStatus();
        this.bookDesc = book.getBookDesc();
    }
}
