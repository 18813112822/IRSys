import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.ansj.lucene3.AnsjAnalysis;
import org.ansj.lucene3.AnsjIndexAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;

public class IRSearcher {
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private HashMap<String, Float> avgLength = new HashMap<String, Float>();
	public static final Set<String> STOPWORDS = new HashSet<String>(Arrays.asList(
				new String[] {" ", "的", "是", "."}
			));

	public IRSearcher(String indexdir) {
		analyzer = new AnsjAnalysis();
//		analyzer = new MyAnalyzer();
//		analyzer = new AnsjIndexAnalysis(true);
		try {
			reader = IndexReader.open(FSDirectory.open(new File(indexdir)));
			searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new SimpleSimilarity());
			loadGlobals("forIndex/global.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TopDocs searchQuery(String queryString, String field, int maxnum) {
		try {
			TokenStream tokenStream  = analyzer.tokenStream(field, new StringReader(queryString));
			CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
			System.out.println(field+" tokens:");
			BooleanQuery query = new BooleanQuery();
			
			ArrayList<String> termstrings = new ArrayList<>();
			while (tokenStream.incrementToken()) {
				String termStr = charTermAttribute.toString();
				if (termstrings.contains(termStr))
					continue;
				termstrings.add(termStr);
			}
			for (String termStr: termstrings){
//				System.out.println("where is true?");
				
//				String termStr = charTermAttribute.toString();
				if (STOPWORDS.contains(termStr))
					continue;
				
//				System.out.println(termStr+"1");
				Query subquery = new SimpleQuery(new Term(field, termStr), avgLength.get(field));
//				Query subquery = new TermQuery(new Term(field, termStr));
				float boost = Float.parseFloat("1.0")/(reader.docFreq(new Term(field, termStr))+1)*termStr.length()/termStr.length();
				subquery.setBoost(boost);
				query.add(subquery, Occur.SHOULD);
				System.out.println(termStr);
			}
			
//			query.setBoost(1.0f);
			//Weight w=searcher.createNormalizedWeight(query);
			//System.out.println(w.getClass());
			TopDocs results = searcher.search(query, maxnum);
//			System.out.println(results);
			return results;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Document getDoc(int docID) {
		try {
			return searcher.doc(docID);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void loadGlobals(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] info = line.split("=");
				avgLength.put(info[0], Float.parseFloat(info[1]));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public float getAvg(String field) {
		return avgLength.get(field);
	}

	public static void main(String[] args) {
		IRSearcher search = new IRSearcher("forIndex/index");
		search.loadGlobals("forIndex/global.txt");
		System.out.println("avg length = " + search.getAvg("title"));

		TopDocs results = search.searchQuery("�����", "title", 100);
		ScoreDoc[] hits = results.scoreDocs;
		for (int i = 0; i < hits.length; i++) { // output raw format
			Document doc = search.getDoc(hits[i].doc);
			System.out.println("doc=" + hits[i].doc + " score="
			                   + hits[i].score + " picPath= " + doc.get("picPath"));
		}
	}
}
