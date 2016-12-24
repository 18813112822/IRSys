
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.contentstream.operator.markedcontent.BeginMarkedContentSequenceWithProperties;
import org.apache.tomcat.jni.OS;
import org.junit.experimental.theories.FromDataPoints;

import com.sun.corba.se.impl.orbutil.ObjectStreamClass_1_3_1;
import com.sun.org.apache.bcel.internal.generic.FLOAD;
import com.sun.org.apache.bcel.internal.generic.INEG;
import com.sun.org.apache.regexp.internal.recompile;
import com.sun.org.apache.xpath.internal.operations.And;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Text;

import javafx.geometry.Pos;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.IntBinaryOperator;
import java.math.*;
import java.net.*;
import java.security.KeyStore.PrivateKeyEntry;
import java.time.chrono.MinguoChronology;
import java.io.*;
import javax.servlet.ServletContext;

public class IRServer extends HttpServlet {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final int PAGE_RESULT = 10;
    public static final String indexDir = "forIndex";
    public static final String picDir = "";
    private IRSearcher search = null;

    public IRServer() {
        super();
        System.out.println("user.dir:" + (System.getProperty("user.dir")));
        search = new IRSearcher(new String(indexDir + "/index"));
        search.loadGlobals(new String(indexDir + "/global.txt"));
    }

    private ScoreDoc[] showList(ScoreDoc[] results, int page) {
        if (results == null || results.length < (page - 1) * PAGE_RESULT) {
            return null;
        }
        int start = Math.max((page - 1) * PAGE_RESULT, 0);
        int docnum = Math.min(results.length - start, PAGE_RESULT);
        ScoreDoc[] ret = new ScoreDoc[docnum];
        for (int i = 0; i < docnum; i++) {
            ret[i] = results[start + i];
        }
        return ret;
    }

    private void normalizeScore(ScoreDoc[] results) {
        if (results.length == 0)
            return;
        float max = results[0].score;
        for (int i = 0; i < results.length; i++) {
            if (results[i].score > max)
                max = results[i].score;
        }
        float muti = (float)1.0 / max;
        for (int i = 0; i < results.length; i++) {
            results[i].score = muti * (results[i].score);
        }
    }

    private ScoreDoc[] multiQuery(Map<String, String> queryInfo, int maxnum) {
        HashMap<String, Float> weight = new HashMap<String, Float>();
        weight.put("题名", (float) 1.0);
        weight.put("英文篇名", (float) 1.0);
        weight.put("摘要", (float) 0.7);
        weight.put("英文摘要", (float) 0.7);


        HashMap<Integer, ScoreDoc> docs = new HashMap<>();

        for (HashMap.Entry<String, Float> entry : weight.entrySet()) {
//          System.out.println(entry.getKey());
            TopDocs results = search.searchQuery(queryInfo, entry.getKey(), 100);
            if (results == null || results.scoreDocs == null)
                continue;
            ScoreDoc[] tmpdocs = results.scoreDocs;
            normalizeScore(tmpdocs);
            for (ScoreDoc document : tmpdocs) {
                if (docs.containsKey(docs.containsKey(document.doc))) {
                    docs.get(document.doc).score += entry.getValue() * document.score;
                } else {
                    docs.put(document.doc, new ScoreDoc(document.doc, entry.getValue() * document.score));
                }
            }
        }


        ScoreDoc[] ans = new ScoreDoc[docs.size()];
        int i = 0;
        for (Map.Entry<Integer, ScoreDoc> entry : docs.entrySet()) {
            ans[i] = entry.getValue();
            i++;
        }
        Arrays.sort(ans, new Comparator<ScoreDoc>() {
            @Override
            public int compare(ScoreDoc o1, ScoreDoc o2) {
                if (o1.score > o2.score)
                    return -1;
                if (o1.score < o2.score)
                    return 1;
                return 0;
            }
        });
        return ans;
    }

    private ArrayList<Integer> allIndexOf(String text, String substring) {
        ArrayList<Integer> pos = new ArrayList<>();
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
//          System.out.println(substring + index);
            pos.add(index);
            index += substring.length();
        }
        return pos;
    }

    private ArrayList<Integer> getSplitIndex(char[] flags) {
        ArrayList<Integer> split = new ArrayList<>();
        if (flags.length == 0) {
            return split;
        }
        split.add(0);
        for (int i = 1; i < flags.length; i++) {
            if (flags[i] != flags[i - 1]) {
                split.add(i);
            }
        }
        split.add(flags.length);
        return split;
    }

    private String highlightText(String text, String querystring) {
        if (text == null)
            return "";
        List<Term> terms = IndexAnalysis.parse(querystring);
        ArrayList<String> arraystrings = new ArrayList<>();
        for (Term term : terms) {
            if (IRSearcher.STOPWORDS.contains(term.toString()))
                continue;
            arraystrings.add(term.toString());
        }
        String[] strings = arraystrings.toArray(new String[0]);

        char[] flags = new char[text.length()];
        for (int i = 0; i < text.length(); i++)
            flags[i] = 0;

        for (String term : strings) {
            ArrayList<Integer> poss = allIndexOf(text, term);
            for (int pos : poss) {
                for (int i = pos; i < pos + term.length(); i++)
                    flags[i] = 1;
            }
        }

        ArrayList<Integer> split = getSplitIndex(flags);
        String ans = "";
        for (int i = 1; i < split.size(); i++) {
            String substring = text.substring(split.get(i - 1), split.get(i));
            char flag = flags[split.get(i - 1)];
            if (flag == 0) {
                int remain = 7;
                String prex = "<span>";
                String subx = "</span>";
                if (substring.length() > remain * 4) {
                    String prefix = substring.substring(0, remain);
                    String suffix = substring.substring(substring.length() - remain, substring.length());
                    substring = prefix + "..." + suffix;
                }
                substring = prex + substring + subx;
            }
            if (flag == 1) {
                String prefix = "<span class = 'highlight'>";
                String suffix = "</span>";
                substring = prefix + substring + suffix;
            }
            ans += substring;
            if (ans.length() > 400)
                break;
        }
        return ans;
    }
    
    private  Map<String, String> getQueryInfo(HttpServletRequest request){
    	HashMap<String, String> queryInfo = new HashMap();
    	
    	String queryString = request.getParameter("query");
    	if (queryString != null && queryString.compareTo("") != 0)
    		queryInfo.put("query", queryString);
    	else
    		queryInfo.put("query", "");
    	
    	String pageString = request.getParameter("page");
    	if (pageString != null && pageString.compareTo("") != 0) {
    		queryInfo.put("page", pageString);
        }
    	else {
			queryInfo.put("page", "1");
		}
    	
    	String authorString = request.getParameter("author");
    	if (authorString != null && authorString.compareTo("") != 0) {
    		queryInfo.put("author", authorString);
        }
    	else
    		queryInfo.put("author", "");
    	
    	String publisherString = request.getParameter("publisher");
    	if (publisherString != null && publisherString.compareTo("") != 0) {
    		queryInfo.put("publisher", publisherString);
        }
    	else
    		queryInfo.put("publisher", "");
    	
    	String startyearString = request.getParameter("startyear");
    	if (startyearString != null) {
    		int startyear;
    		try {
    			startyear = Integer.valueOf(startyearString);
			} catch (Exception e) {
				startyear = 1901;
			}
    		queryInfo.put("startyear", String.valueOf(startyear));
        }
    	
    	String endyearString = request.getParameter("endyear");
    	if (endyearString != null) {
    		int endyear;
    		try {
    			endyear = Integer.valueOf(endyearString);
    			queryInfo.put("endyear", String.valueOf(endyear));
			} catch (Exception e) {
				endyear = 2020;
			}
    		queryInfo.put("endyear", String.valueOf(endyear));
        }
    	return queryInfo;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        
        Map<String, String> queryInfo = getQueryInfo(request);
        
        String queryString = queryInfo.get("query");
        int page = Integer.valueOf(queryInfo.get("page"));
       
        System.out.println(queryString);
        System.out.println(URLDecoder.decode(queryString, "utf-8"));
        System.out.println(URLDecoder.decode(queryString, "gb2312"));

        String[] titles = null;
        String[] titles_en = null;
        String[] abstracts = null;
        String[] authors = null;
        String[] publishers = null;
        String[] years = null;
        
//            ScoreDoc[] results = multiQuery(queryString, 100);
        TopDocs docresults = search.searchQuery(queryInfo, "", 100);
        
        ScoreDoc[] results = null;
        if (docresults != null)
            results = docresults.scoreDocs;
        if (results != null) {
            ScoreDoc[] hits = showList(results, page);
            if (hits != null) {
                System.out.println(hits.length);
                titles = new String[hits.length];
                titles_en = new String[hits.length];
                abstracts = new String[hits.length];
                authors = new String[hits.length];
                publishers = new String[hits.length];
                years = new String[hits.length];
                
                for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
                    Document doc = search.getDoc(hits[i].doc);
                    titles[i] = doc.get("题名");
                    if (titles[i] == null)
                        titles[i] = "";
                    titles_en[i] = doc.get("英文篇名");
                    if (titles_en[i] == null)
                        titles_en[i] = "";
                    abstracts[i] = doc.get("摘要");
                    if (abstracts[i] == null)
                        abstracts[i] = "";
                    authors[i] = doc.get("作者");
                    if (authors[i] == null)
                        authors[i] = "";
                    publishers[i] = doc.get("出版单位");
                    if (publishers[i] == null)
                        publishers[i] = "";
                    years[i] = doc.get("年");
                    if (years[i] == null)
                        years[i] = "";
                    
                    abstracts[i] = highlightText(abstracts[i], queryString).replaceAll("\n\r", " ").replaceAll("\n", " ");
                    System.out.println(abstracts[i]);
                    System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score + " title=" + doc.get("题名"));
                }

            } else {
                System.out.println("page null");
            }
        } else {
            System.out.println("result null");
        }
        request.setAttribute("currentQuery", queryString);
        request.setAttribute("currentPage", page);
        request.setAttribute("currentAuthor", queryInfo.get("author"));
        request.setAttribute("currentPublisher", queryInfo.get("publisher"));
        request.setAttribute("currentStartyear", queryInfo.get("startyear"));
        request.setAttribute("currentEndyear", queryInfo.get("endyear"));
        request.setAttribute("titles", titles);
        request.setAttribute("titles_en", titles_en);
        request.setAttribute("abstracts", abstracts);
        request.setAttribute("authors", authors);
        request.setAttribute("publishers", publishers);
        request.setAttribute("years", years);
        request.getRequestDispatcher("/show.jsp").forward(request,
                response);

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        this.doGet(request, response);
    }
}
