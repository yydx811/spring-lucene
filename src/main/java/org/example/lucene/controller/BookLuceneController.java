package org.example.lucene.controller;

import org.apache.lucene.search.SortField;
import org.example.lucene.domain.Book;
import org.example.lucene.domain.Page;
import org.example.lucene.domain.QueryParam;
import org.example.lucene.service.BookLuceneService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class BookLuceneController {

    @Resource
    BookLuceneService bookLuceneService;

    @GetMapping("/book/index/reload")
    public void reloadBookIndex() {
        bookLuceneService.reloadDoc();
    }

    @GetMapping("/book/index/get")
    public Book get(@RequestParam("id") Long id) {
        return bookLuceneService.findById(id);
    }

    @GetMapping("/book/index/batch")
    public List<Book> get(@RequestParam("ids") List<Long> ids, @RequestParam("reverse") boolean reverse) {
        return bookLuceneService.findByIds(ids, new QueryParam.Sort("id", reverse, SortField.Type.LONG));
    }

    @PostMapping(value = "/book/index/add", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public void add(@RequestBody Book book) {
        bookLuceneService.add(book);
    }

    @PostMapping(value = "/book/index/update", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public void update(@RequestBody List<Book> books) {
        bookLuceneService.update(books);
    }

    @PostMapping(value = "/book/index/query", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public Page<Book> query(@RequestBody QueryParam param) {
        return bookLuceneService.query(param);
    }
}
