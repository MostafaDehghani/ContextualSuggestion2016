/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.DataHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import nl.uva.lucenefacility.Indexer;
import nl.uva.lucenefacility.MyAnalyzer;
import static nl.uva.settings.Config.configFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.SimpleFSDirectory;

/**
 *
 * @author Mostafa Dehghani
 */
public class Indexing extends Indexer {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Indexing.class.getName());

    public Indexing() throws Exception, Throwable {
        super(configFile.getProperty("INDEX_PATH"));
    }

    @Override
    protected void docIndexer() throws Exception {

        try {
            String CorpusPathString = configFile.getProperty("FILES_PATHS");
            File root = new File(CorpusPathString);
            List<File> files = (List<File>) FileUtils.listFiles(root, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            for (File file : files) {
                if(file.getName().equals(".DS_Store"))
                    continue;
                TextFile tFile = new TextFile(file, root);
                this.IndexDoc(tFile);
            }
        } catch (Exception ex) {
            log.error(ex);
            throw ex;
        }
        System.out.println("number of indexed docs: " + docCount);
    }

    @Override
    protected void analyzerMapInitializer(Map<String, Analyzer> analyzerMap) {
        analyzerMap.put("ID", new KeywordAnalyzer());//StandardAnalyzer(Version.LUCENE_CURRENT));
        analyzerMap.put("CITY", new KeywordAnalyzer());//StandardAnalyzer(Version.LUCENE_CURRENT));
//        analyzerMap.put("ID", new MyAnalyzer(Boolean.FALSE).ArbitraryCharacterAsDelimiterAnalyzer('/'));
//        analyzerMap.put("PATH", new MyAnalyzer(Boolean.FALSE).ArbitraryCharacterAsDelimiterAnalyzer('/'));
    }

    @Override
    protected void IndexDoc(Object obj) throws Exception {
        TextFile tf = (TextFile) obj;
        Document doc = new Document();
        Integer fileLength = tf.Content.split("\\s+").length;
        if (fileLength < minDocLength) //Filtering small documents
        {
            log.info("File " + tf.PathFromRoot + " is skeeped due to min length constraint: File Length=" + fileLength );
            return;
//            tf.Content = "bllllllaaaaa bllllllaaaaa bllllllaaaaa";
//            log.info("Content of file " + tf.PathFromRoot + " is changed to \"" +tf.Content+  "\" due to min length constraint: File Length=" + fileLength );
        }
        String id = tf.FileName.replace(".txt","");
        String cityCode =  id.substring(id.length() - 3);
        System.out.println("citycode: " + cityCode);
        doc.add(new Field("ID", id , Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
        doc.add(new Field("CITY", cityCode , Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
        doc.add(new Field("TEXT", tf.Content, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
        try {
            writer.addDocument(doc);
        } catch (IOException ex) {
            log.error(ex);
        }
//        log.info("Douc number: " + docCount + " - Document " + tf.PathFromRoot + " has been indexed successfully...");
        docCount++;
        if(docCount%1000 ==0){
            System.out.println("doc num:" + docCount);
            log.info(docCount);
        }
            
    }
    
    @Override
    public void Indexer(String indexPathString) throws Exception, Throwable {
        try {
            log.info("----------------------- INDEXING - Override Version: for BM25 :)  --------------------------");

            Path ipath = FileSystems.getDefault().getPath(indexPathString);
            super.IndexesCleaner(indexPathString);
            MyAnalyzer myAnalyzer;
            if (!super.stopWordsRemoving)
                myAnalyzer = new MyAnalyzer(super.stemming);
            else
                myAnalyzer = new MyAnalyzer(super.stemming, super.LoadStopwords());
            
            Analyzer analyzer = myAnalyzer.getAnalyzer(configFile.getProperty("CORPUS_LANGUAGE"));
            PerFieldAnalyzerWrapper prfWrapper = new PerFieldAnalyzerWrapper(analyzer, super.analyzerMap);
            IndexWriterConfig irc = new IndexWriterConfig(prfWrapper);
            irc.setSimilarity(new BM25Similarity(1.2F, 0.75F));
            this.writer = new IndexWriter(new SimpleFSDirectory(ipath), irc);
            this.docIndexer();
            this.writer.commit();
            this.writer.close();
            analyzer.close();
            prfWrapper.close();
            log.info("-------------------------------------------------");
            log.info("Index is created successfully...");
            log.info("-------------------------------------------------");

        } catch (Exception ex) {
            log.error(ex);
            throw ex;
        }
    }

    public static void main(String[] args) throws Exception, Throwable {
//        Indexing di = new Indexing();
    }
}

class TextFile{

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TextFile.class.getName());
    public String FileName;
    public String PathFromRoot;
    public String Content;

    public TextFile(File file, File root) throws IOException {
        if(file.getName().contains(".warc.gz"))
            this.FileName = file.getName().replace(".warc.gz", "");
        else
            this.FileName = file.getName();
        this.PathFromRoot = file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(root.getName()));
        this.Content = readFileAsString(file);
    }    
    public static String readFileAsString(String filePath) throws IOException {
        return readFileAsString(new File(filePath));
    }

    public static String readFileAsString(File file) throws IOException {
        BufferedInputStream f = null;
        String str = "";
        try {
            byte[] buffer = new byte[(int) file.length()];
            f = new BufferedInputStream(new FileInputStream(file));
            f.read(buffer);
            str = new String(buffer);
        } catch (FileNotFoundException ex) {
            log.error(ex);
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        } finally {
            try {
                f.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }
        return str;
    }
}