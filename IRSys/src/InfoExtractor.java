import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sun.java.swing.plaf.windows.resources.windows_pt_BR;

import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hslf.record.TxInteractiveInfoAtom;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XSLFSlideShow;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class InfoExtractor {
	
	static String[] FIELDS = new String[]{
		"title", "h1", "h2", "h3", "h4", "h5", "h6", "text"
	};
	
//	static String ROOTDIR = "../BuildIndex/mirrors/";
	static String ROOTDIR = "/Users/shisy13/Desktop/mirrors/";
	
	static private String extractWebTag(String tag, Document document){
		Elements eles = document.getElementsByTag(tag);
 		ArrayList<String> strs = new ArrayList<String>();
		for (Element ele : eles){
			if (strs.contains(ele.text()))
				continue;
			strs.add(ele.text());
//			System.out.println(ele.text());
		}
//		System.out.println(String.join("  ", strs).length());
		return String.join("  ", strs);
	}
	
	
	static private HashMap<String, String> extractWeb(String filename, String charset){
		HashMap<String, String> info = new HashMap<String, String>(){
			{
				for (String field : FIELDS){
					put(field, "");
				}
			}
		};
		File file = new File(filename);
		try {
			Document doc = Jsoup.parse(file, charset);
			for (String tag : FIELDS){
				if (tag.equals("text"))
					info.put("text", doc.text());
				else{	
					String str = extractWebTag(tag, doc);
					info.put(tag, str);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return info;
	}
	
	static private HashMap<String, String> extractPdf(String filename, String charset){
		HashMap<String, String> info = new HashMap<String, String>(){
			{
				for (String field : FIELDS){
					put(field, "");
				}
			}
		};
		
		PDDocument document;
		try {
			document = PDDocument.load(new File(filename));
			PDFTextStripper pdfTextStripper = new PDFTextStripper();
			String text = pdfTextStripper.getText(document);
			info.put("text", text);
//			String title = filename.substring(new String(ROOTDIR).length());
//			info.put("title", title);
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return info;
	}
	
	static private HashMap<String, String> extractDoc(String filename, String charset){
		HashMap<String, String> info = new HashMap<String, String>(){
			{
				for (String field : FIELDS){
					put(field, "");
				}
			}
		};
//		System.out.println(filename);
		try {
			InputStream stream = new FileInputStream(new File(filename));
			String text = "";
			if (filename.endsWith(".doc")){
				HWPFDocument hwpfDocument = new HWPFDocument(stream);
				WordExtractor extractor = new WordExtractor(hwpfDocument);
				text = extractor.getText();
				extractor.close();
			}
			else {
				XWPFDocument xwpfDocument = new XWPFDocument(stream);
				XWPFWordExtractor extractor = new XWPFWordExtractor(xwpfDocument);
				text = extractor.getText();
				extractor.close();
			}
			
			info.put("text", text);
//			String title = filename.substring(new String(ROOTDIR).length());
//			info.put("title", title);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}
	
	static private HashMap<String, String> extractPpt(String filename, String charset){
		HashMap<String, String> info = new HashMap<String, String>(){
			{
				for (String field : FIELDS){
					put(field, "");
				}
			}
		};
//		System.out.println(filename);
		try {
//			InputStream stream = new FileInputStream(new File(filename));
			String text = "";
			if (filename.endsWith(".ppt")){
//				HSLFSlideShow pptdocument = new HSLFSlideShow(stream);
				PowerPointExtractor extractor = new PowerPointExtractor(filename);
				text = extractor.getText();
				extractor.close();
			}
			else {
				XSLFSlideShow slideshow = new XSLFSlideShow(filename);
				XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(slideshow);
				text = extractor.getText();
				extractor.close();
			}
			
			info.put("text", text);
//			System.out.println(text);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}
	
	static private HashMap<String, String> extractXls(String filename, String charset){
		HashMap<String, String> info = new HashMap<String, String>(){
			{
				for (String field : FIELDS){
					put(field, "");
				}
			}
		};
//		System.out.println(filename);
		try {
			InputStream stream = new FileInputStream(new File(filename));
			String text = "";
			if (filename.endsWith(".xls")){
				HSSFWorkbook workbook = new HSSFWorkbook(stream);
				ExcelExtractor extractor = new ExcelExtractor(workbook);
				text = extractor.getText();
				extractor.close();
			}
			else {
				XSSFWorkbook workbook = new XSSFWorkbook(stream);
				XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook);
				text = extractor.getText();
				extractor.close();
			}
			
			info.put("text", text);
//			System.out.println(text);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}

	
	static public HashMap<String, String> extractInfo(String filename, String charset, String ftype){
		HashMap<String, String> info = new HashMap<String, String>(){
			{
				for (String field : FIELDS){
					put(field, "");
				}
			}
		};
		System.out.println(filename);
		if (ftype.equals("html"))
			return extractWeb(filename, charset);
		else if (ftype.equals("pdf"))
			return extractPdf(filename, charset);
		else if (ftype.equals("doc"))
			return extractDoc(filename, charset);
		else if (ftype.equals("ppt"))
			return extractPpt(filename, charset);
//		else if (ftype.equals("xls"))
//			return extractXls(filename, charset);
		return info;
	}

	 public static void main(String[] args) {
		 String filename = "bad.docx";
		 String charset = "utf-8";
		 extractDoc(filename, charset);
	 }
}
