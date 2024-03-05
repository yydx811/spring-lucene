package org.example.lucene.config;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Paths;

@Configuration
public class BookLuceneConfig {

    @Value("${lucene.index.path:index/book}")
    String indexPath;

    @Bean
    public Directory directory() throws IOException {
        return MMapDirectory.open(Paths.get(indexPath));
    }

    @Bean
    public Analyzer analyzer() {
        return new SmartChineseAnalyzer();
    }

    @Bean
    public IndexWriter indexWriter(Directory directory, Analyzer analyzer) throws IOException {
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        return new IndexWriter(directory, writerConfig);
    }

    @Bean
    public SearcherManager searcherManager(IndexWriter indexWriter) throws IOException {
        SearcherManager manager = new SearcherManager(indexWriter, false, false, new SearcherFactory());
        ControlledRealTimeReopenThread thread = new ControlledRealTimeReopenThread(indexWriter, manager, 5.0d, 0.025d);
        thread.setDaemon(true);
        thread.start();
        return manager;
    }
}
