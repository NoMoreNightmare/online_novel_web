package com.java2nb.novel.controller.page;

import com.java2nb.novel.core.bean.UserDetails;
import com.java2nb.novel.core.exception.ChapterNotExistException;
import com.java2nb.novel.core.utils.CookieUtil;
import com.java2nb.novel.core.utils.JwtTokenUtil;
import com.java2nb.novel.core.utils.ThreadLocalUtil;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookContent;
import com.java2nb.novel.entity.BookIndex;
import com.java2nb.novel.service.MyBookService;
import com.java2nb.novel.service.MyUserService;
import com.java2nb.novel.vo.BookCommentVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Controller
@RequestMapping("book")
public class NonRestBookController {
    @Resource
    private MyBookService bookService;
    @Resource
    private JwtTokenUtil jwtTokenUtil;
    @Resource
    private MyUserService myUserService;

    @SneakyThrows
    @RequestMapping("{id}.html")
    public String bookDetails(@PathVariable("id") long id, Model model) {
        Book book = CompletableFuture.supplyAsync(() -> {
            //查询book的完整信息
            return bookService.queryBook(id);
        }).get();

        PageBean<BookCommentVO> bookComment = CompletableFuture.supplyAsync(() -> {
            //查询这个book的评论信息bookCommentPageBean
            return bookService.queryBookComment(id, 1, 10);
        }).get();

        //查询这个book的同类书籍信息recBooks
        List<Book> recBooks = CompletableFuture.supplyAsync(() -> bookService.queryRecommendedBooks(id)).get();

        //查询这个book的首章节信息
        Long chapterId = CompletableFuture.supplyAsync(() -> bookService.queryBookLastChapter(id)).get();

        model.addAttribute("book", book);
        model.addAttribute("bookCommentPageBean", bookComment);
        model.addAttribute("recBooks", recBooks);
        model.addAttribute("firstBookIndexId", chapterId);

        return "book/book_detail";

    }

    @SneakyThrows
    @GetMapping("{bookId}/{bookIndexId}.html")
    public String bookContent(@PathVariable("bookId") long bookId, @PathVariable("bookIndexId") long bookIndexId, Model model,
                              HttpServletRequest request) {
        //书的相关信息
        Book book = CompletableFuture.supplyAsync(() -> bookService.queryBook(bookId)).get();
        //书的目录信息
        CompletableFuture<BookIndex> bookIndexCompletableFuture = CompletableFuture.supplyAsync(() -> bookService.queryAboutCurrentIndex(bookId, bookIndexId));
        CompletableFuture<Long> nextChapterIdCompletableFuture = bookIndexCompletableFuture.thenApply(bookIndex -> bookService.queryBookIndexIdByIndexNum(bookId, bookIndex.getIndexNum() + 1));
        CompletableFuture<Long> preChapterIdCompletableFuture = bookIndexCompletableFuture.thenApply(bookIndex -> bookService.queryBookIndexIdByIndexNum(bookId, bookIndex.getIndexNum() - 1));


        BookIndex bookIndex = bookIndexCompletableFuture.get();
        if(bookIndex == null) {
            throw new ChapterNotExistException();
        }

//        Long nextChapterId = bookService.queryBookIndexIdByIndexNum(bookId, bookIndex.getIndexNum() + 1);
//        Long preChapterId = bookService.queryBookIndexIdByIndexNum(bookId, bookIndex.getIndexNum() - 1);

        Long nextChapterId = nextChapterIdCompletableFuture.get();
        Long preChapterId = preChapterIdCompletableFuture.get();

        //书的章节目录内容
        BookContent bookContent = CompletableFuture.supplyAsync(() -> bookService.queryBookContent(bookId, bookIndexId)).get();

        //判断是否需要购买
        boolean needBuy = false;
        //1、判断当前章节是否是vip章节
        Byte isVip = bookIndex.getIsVip();
        if(isVip == null || isVip == 0){
            //不是vip章节
            needBuy = false;
        }else{
            //2、是否登录，未登录一定不能看vip章节
            String token = CookieUtil.getCookie(request, "Authorization");
            if(token == null) {
                token = request.getHeader("Authorization");
            }

            if(token != null && jwtTokenUtil.canRefresh(token)) {
                UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
                //3、已登录，则查看是否购买了本书当前章节的vip，未购买则不能看
                long userId = userDetails.getId();
                long records = myUserService.queryUserBuyRecord(userId, bookIndexId);
                //4、可以看
                needBuy = (records == 0);

                if(!needBuy){
                    //更新用户的阅读历史
                    myUserService.updateReadHistory(bookId, userId, bookContent.getId());
                }

            }else{
                needBuy = true;
            }
        }

        model.addAttribute("book", book);
        model.addAttribute("bookIndex", bookIndex);
        model.addAttribute("nextBookIndexId", nextChapterId);
        model.addAttribute("preBookIndexId", preChapterId);
        model.addAttribute("bookContent", bookContent);
        model.addAttribute("needBuy", needBuy);
        return ThreadLocalUtil.getTemplateDir() + "book/book_content";
    }

    @GetMapping("indexList-{bookId}.html")
    @SneakyThrows
    public String indexList(@PathVariable("bookId") long bookId, Model model) {
        Book book = CompletableFuture.supplyAsync(() -> bookService.queryBook(bookId)).get();
//        Book book = bookService.queryBook(bookId);
        List<BookIndex> bookIndex = CompletableFuture.supplyAsync(() -> bookService.queryAllIndex(bookId)).get();
//        List<BookIndex> bookIndex = bookService.queryAllIndex(bookId);
        model.addAttribute("book", book);
        model.addAttribute("bookIndexCount", bookIndex.size());
        model.addAttribute("bookIndexList", bookIndex);

        return ThreadLocalUtil.getTemplateDir() + "book/book_index";
    }

    @GetMapping("comment-{bookId}.html")
    public String comment(@PathVariable("bookId") long bookId, Model model) {
        Book book = bookService.queryBook(bookId);
        model.addAttribute("book", book);
        return ThreadLocalUtil.getTemplateDir() + "book/book_comment";

    }

    @RequestMapping("bookclass.html")
    public String bookClass() {
        return "book/bookclass";
    }

    @RequestMapping("book_ranking.html")
    public String bookRank() {
        return "book/book_ranking";
    }


}
