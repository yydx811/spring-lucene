package org.example.lucene.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.example.lucene.domain.Book;
import org.example.lucene.domain.Page;
import org.example.lucene.domain.QueryParam;
import org.example.lucene.exception.BookLuceneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class BookLuceneService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookLuceneService.class);

    @Value("${lucene.doc.path}")
    String docPath;
    @Resource
    IndexWriter indexWriter;
    @Resource
    SearcherManager searcherManager;
    @Resource
    Analyzer analyzer;

    public void reloadDoc() {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(ResourceUtils.getFile(docPath), "r");
            List<Book> books = new ArrayList<>();
            String line = file.readLine();
            while (line != null) {
                String str = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                String[] array = str.split(",");
                try {
                    books.add(new Book(Long.parseLong(array[0]), array[1], array[2], Long.parseLong(array[3])));
                } catch (Exception e) {
                    LOGGER.error("unexpected line: {}.", str);
                }
                line = file.readLine();
            }

            indexWriter.deleteAll();
            indexWriter.commit();

            if (!books.isEmpty()) {
                add(books);
            }
        } catch (FileNotFoundException e) {
            throw new BookLuceneException("book doc not found! path: " + docPath + ".", e);
        } catch (IOException e) {
            throw new BookLuceneException(e);
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    LOGGER.error("book doc close failure!", e);
                }
            }
        }
    }

    public void add(List<Book> books) {
        List<Document> newDocs = new ArrayList<>(books.size());
        for (Book book : books) {
            newDocs.add(toDoc(book));
        }
        try {
            indexWriter.addDocuments(newDocs);
            indexWriter.commit();
        } catch (IOException e) {
            throw new BookLuceneException("add books failure!", e);
        }
    }

    public void add(Book book) {
        try {
            indexWriter.addDocument(toDoc(book));
            indexWriter.commit();
        } catch (IOException e) {
            throw new BookLuceneException("add book failure!", e);
        }
    }

    public void update(List<Book> books) {
        for (Book book : books) {
            try {
                long seqNum = indexWriter.updateDocument(new Term("id", String.valueOf(book.getId())), toDoc(book));
                System.out.println(seqNum);
            } catch (IOException e) {
                throw new BookLuceneException("update book failure, id: " + book.getId() + ".", e);
            }
        }
    }

    public Book findById(Long id) {
        try {
            searcherManager.maybeRefresh();
            IndexSearcher searcher = searcherManager.acquire();
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(LongPoint.newExactQuery("id", id), BooleanClause.Occur.MUST);
            TopDocs topDocs = searcher.search(builder.build(), 1);
            if (topDocs.totalHits <= 0 || topDocs.scoreDocs.length == 0) {
                return null;
            }
            return toPojo(searcher.doc(topDocs.scoreDocs[0].doc));
        } catch (IOException e) {
            throw new BookLuceneException("find by id failure!", e);
        }
    }

    public List<Book> findByIds(List<Long> ids, QueryParam.Sort querySort) {
        try {
            searcherManager.maybeRefresh();
            IndexSearcher searcher = searcherManager.acquire();
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            for (Long id : ids) {
                builder.add(LongPoint.newExactQuery("id", id), BooleanClause.Occur.SHOULD);
            }
            Sort sort = new Sort();
            if (querySort != null) {
                sort.setSort(new SortField(querySort.getField(), querySort.getType(), querySort.getReverse()));
            }
            TopDocs topDocs = searcher.search(builder.build(), ids.size(), sort);
            if (topDocs.totalHits <= 0 || topDocs.scoreDocs.length == 0) {
                return null;
            }
            return Stream.of(topDocs.scoreDocs).map(scoreDoc -> {
                try {
                    return searcher.doc(scoreDoc.doc);
                } catch (IOException e) {
                    throw new BookLuceneException(e);
                }
            }).map(this::toPojo).collect(Collectors.toList());
        } catch (IOException e) {
            throw new BookLuceneException("find by ids failure!", e);
        }
    }

    public Page<Book> query(QueryParam query) {
        try {
            searcherManager.maybeRefresh();
            IndexSearcher searcher = searcherManager.acquire();
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            if (query.getId() != null) {
                builder.add(LongPoint.newExactQuery("id", query.getId()), BooleanClause.Occur.MUST);
            }
            if (!StringUtils.isEmpty(query.getTitle())) {
                builder.add(new QueryParser("title", analyzer).parse(query.getTitle()), BooleanClause.Occur.MUST);
            }
            if (!CollectionUtils.isEmpty(query.getStatusList())) {
                BooleanQuery.Builder statusBuilder = new BooleanQuery.Builder();
                for (String status : query.getStatusList()) {
                    statusBuilder.add(new TermQuery(new Term("status", status)), BooleanClause.Occur.SHOULD);
                }
                builder.add(statusBuilder.build(), BooleanClause.Occur.MUST);
            }
            if (query.getStartTime() != null || query.getEndTime() != null) {
                long min = query.getStartTime() == null ? Long.MIN_VALUE : query.getStartTime();
                long max = query.getEndTime() == null ? Long.MAX_VALUE : query.getEndTime();
                builder.add(LongPoint.newRangeQuery("time", min, max), BooleanClause.Occur.MUST);
            }
            Sort sort = new Sort();
            if (!CollectionUtils.isEmpty(query.getSorts())) {
                for (QueryParam.Sort querySort : query.getSorts()) {
                    sort.setSort(new SortField(querySort.getField(), querySort.getType(), querySort.getReverse()));
                }
            }
            int start = (query.getPageNum() - 1) * query.getPageSize();
            int end = query.getPageNum() * query.getPageSize();
            TopDocs topDocs = searcher.search(builder.build(), end, sort);
            if (topDocs.totalHits <= 0 || topDocs.scoreDocs.length == 0 || topDocs.totalHits < start) {
                return new Page<>(null, topDocs.totalHits, query.getPageNum(), query.getPageSize());
            }

            List<Book> books = IntStream.range(start, Math.min(end, topDocs.scoreDocs.length)).boxed()
                    .map(i -> topDocs.scoreDocs[i].doc)
                    .map(x -> {
                        try {
                            return searcher.doc(x);
                        } catch (IOException e) {
                            throw new BookLuceneException(e);
                        }
                    })
                    .map(this::toPojo).collect(Collectors.toList());
            return new Page<>(books, topDocs.totalHits, query.getPageNum(), query.getPageSize());
        } catch (IOException | ParseException e) {
            throw new BookLuceneException("query books failure!", e);
        }
    }

    public Book toPojo(Document doc) {
        return new Book(Long.parseLong(doc.get("id")), doc.get("title"), doc.get("status"), Long.parseLong(doc.get("time")));
    }

    public Document toDoc(Book book) {
        Document doc = new Document();
        doc.add(new LongPoint("id", book.getId()));
        doc.add(new NumericDocValuesField("id", book.getId()));
        doc.add(new StoredField("id", book.getId()));
        doc.add(new TextField("title", book.getTitle(), Field.Store.YES));
        doc.add(new StringField("status", book.getStatus(), Field.Store.YES));
        doc.add(new LongPoint("time", book.getTime()));
        doc.add(new NumericDocValuesField("time", book.getTime()));
        doc.add(new StoredField("time", book.getTime()));
        return doc;
    }
}
