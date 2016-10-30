import java.io.*;
import java.util.HashMap;

import org.w3c.dom.*;

import org.ansj.lucene3.AnsjAnalysis;
import org.ansj.lucene3.AnsjIndexAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import javax.el.MapELResolver;
import javax.xml.parsers.*;

public class IRIndexer {
    private Analyzer analyzer;
    private IndexWriter indexWriter;
    private HashMap<String, Float> avgLength = new HashMap<String, Float>();
    
    private int MaxTitleLength = 30;
    
    public IRIndexer(String indexDir) {
//      analyzer = new AnsjAnalysis();
    	analyzer = new AnsjIndexAnalysis();
//    	analyzer = new MyAnalyzer();
        try {
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, analyzer);
            Directory dir = FSDirectory.open(new File(indexDir));
            indexWriter = new IndexWriter(dir, iwc);
            indexWriter.setSimilarity(new SimpleSimilarity());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void saveGlobals(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new File(filename));
            
            for (HashMap.Entry<String, Float> entry: avgLength.entrySet()){
            	pw.println(entry.getKey()+"="+entry.getValue());
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * index sogou.xml
     *
     */
    public void indexSpecialFile(String filename) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(new File(filename));
            
            NodeList nodeList = doc.getElementsByTagName("web");
            for (int i = 0; i < nodeList.getLength(); i++){
            	Node node = nodeList.item(i);
            	NamedNodeMap map = node.getAttributes();
            	Document document = new Document();
            	
            	String subtitle = map.getNamedItem("title").getNodeValue();
            	String pagerank = map.getNamedItem("pagerank").getNodeValue();
            	document.add(new Field("pagerank", pagerank, Field.Store.YES, Field.Index.NO));
            	
            	Node url = map.getNamedItem("url");
            	Field webUrlField = new Field("url", url.getNodeValue(), Field.Store.YES, Field.Index.ANALYZED);
            	document.add(webUrlField);
            	if (avgLength.get("url") == null)
            		avgLength.put("url", (float) url.getNodeValue().length());
            	else
            		avgLength.put("url", avgLength.get("url") + url.getNodeValue().length());
            	
            	String filepath = url.getNodeValue();
            	String charset = map.getNamedItem("charset").getNodeValue();
            	String ftype = map.getNamedItem("type").getNodeValue();
            	HashMap<String, String> fileinfo = InfoExtractor.extractInfo(InfoExtractor.ROOTDIR+filepath, charset, ftype);
            	if (fileinfo == null)
            		continue;
            	
            	if (!subtitle.equals("")){
            		if (!fileinfo.get("title").equals(subtitle))
	            		if (!fileinfo.get("title").equals(""))
	            			fileinfo.put("title", fileinfo.get("title") + " - " + subtitle);
            		else
            			fileinfo.put("title", subtitle);
            	}
            	
            	if (fileinfo.get("title").equals(""))
            		fileinfo.put("title", url.getNodeValue());
            	
//            	if (fileinfo.get("title").length() > MaxTitleLength)
//            		fileinfo.put("title", fileinfo.get("title").substring(0, MaxTitleLength) + "...");
            	
            	for (String field : InfoExtractor.FIELDS){
            		Field newfield = new Field(field, fileinfo.get(field), Field.Store.YES, Field.Index.ANALYZED);
            		document.add(newfield);
            		if (avgLength.get(field) == null)
            			avgLength.put(field, (float) fileinfo.get(field).length());
            		else
            			avgLength.put(field, avgLength.get(field) + fileinfo.get(field).length());
            	}

            	indexWriter.addDocument(document);
            }
            
            for (HashMap.Entry<String, Float> entry: avgLength.entrySet()){
            	avgLength.put(entry.getKey(), entry.getValue()/indexWriter.numDocs());
            }
            
            System.out.println("total " + indexWriter.numDocs() + " documents");
            indexWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        IRIndexer indexer = new IRIndexer("forIndex/index");
        indexer.indexSpecialFile("input/campus.xml");
        indexer.saveGlobals("forIndex/global.txt");
    }
}
