package com.analyze.ko.framework.server.servlet;

import com.analyze.ko.framework.korean.DataClass.Document;
import com.analyze.ko.framework.korean.DataClass.SentimentTypeInterface;
import com.analyze.ko.framework.korean.DataClass.WordInfo;
import com.analyze.ko.framework.korean.managerclass.WordInfoManager;
import com.analyze.ko.framework.server.util.FileIO;
import com.twitter.penguin.korean.TwitterKoreanProcessorJava;
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer;
import org.json.JSONArray;
import org.json.JSONObject;
import scala.collection.Seq;
import scala.collection.convert.WrapAsJava$;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by parkjaesung on 2016. 7. 24..
 */
public class AnalyzeSetenceJSP extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FileIO fileIO = FileIO.getInstance();
        String currentDirectory = System.getProperty("user.dir");
        String idx = req.getParameter("idx");
        String outPutFileDirectory = currentDirectory + "/KoAnalyzer-server/src/main/resources/DataDocuments/"+ idx +".txt";
        StringBuilder sb = new StringBuilder();
        String docText = "";

        System.out.println("AnalyzeSetenceJSP doGet");

        if(req.getParameter("idx")!= null){
        docText = fileIO.readFile(outPutFileDirectory);
        }else if(req.getParameter("q")!=null){
            docText = req.getParameter("q");
        }else{
            return;
        }


        //Create Document Object and analzye
        Document document = new Document();
        document.setTextData(docText);
        document.removeSpecialChar();
        document.setTextLength();
        document.setSentiment();

        WordInfoManager wordInfoManager = document.getWordInfoManager();

        ArrayList wordInfos = wordInfoManager.mapToArrayList();

        //Analyze pos
        //SearchCorpusData object for searching word in corpus data
        Seq<KoreanTokenizer.KoreanToken> tokens = getKoreanTokensFromText(docText);

        System.out.println(TwitterKoreanProcessorJava.tokensToJavaKoreanTokenList(tokens));
        // [한국어(Noun: 0, 3), 를(Josa: 3, 1),  (Space: 4, 1), 처리(Noun: 5, 2), 하는(Verb: 7, 2),  (Space: 9, 1), 예시(Noun: 10, 2), 입니(Adjective: 12, 2), 다(Eomi: 14, 1), ㅋㅋ(KoreanParticle: 15, 2),  (Space: 17, 1), #한국어(Hashtag: 18, 4)]

        //Redirect JSP
        RequestDispatcher rd;
        rd = getServletContext().getRequestDispatcher("/AnalyzeDocument.jsp");

        //setAttribute to JSP
        System.out.println("doctText : " + docText);
        req.setAttribute("docText", docText);
        req.setAttribute("docLength", docText.length());
        req.setAttribute("densityWordsJSON", densitySortWordsToJSON(wordInfos));
        req.setAttribute("posAnalyzed", TwitterKoreanProcessorJava.tokensToJavaKoreanTokenList(tokens).toString());

        rd.forward(req, resp);
    }

    private String densitySortWordsToJSON(ArrayList wordInfos){
        //Convert density word arraylist to JSON
        JSONObject densityJSON = new JSONObject();
        JSONObject densityChartJSON = new JSONObject();
        JSONArray densityDataJSONArray = new JSONArray();

        densityChartJSON.put("caption", "DensityKeyWord");
        densityChartJSON.put("theme", "ocean");
        densityJSON.put("chart", densityChartJSON);

        for (int i = 0; i < wordInfos.size(); i++) {
            WordInfo wordInfo = (WordInfo) wordInfos.get(i);

            SentimentTypeInterface.SentimentType sentimentType = wordInfo.getSentimentType();
            if(sentimentType != SentimentTypeInterface.SentimentType.NEUT && sentimentType != SentimentTypeInterface.SentimentType.NODATA){
            JSONObject densityDataJSON = new JSONObject();

            densityDataJSON.put("label", wordInfo.getWord());
            densityDataJSON.put("value", wordInfo.getDensity());

            densityDataJSONArray.put(densityDataJSON);
            }
        }

        densityJSON.put("data", densityDataJSONArray);

        return densityJSON.toString();

    }

    private List<KoreanTokenizer.KoreanToken> convertToList(Seq<KoreanTokenizer.KoreanToken> seq) {
        return WrapAsJava$.MODULE$.seqAsJavaList(seq);
    }

    private Seq<KoreanTokenizer.KoreanToken> getKoreanTokensFromText(String text){
        // Normalize
        CharSequence normalized = TwitterKoreanProcessorJava.normalize(text);
        System.out.println(normalized);

        Seq<KoreanTokenizer.KoreanToken> tokens = TwitterKoreanProcessorJava.tokenize(normalized);

        return tokens;
    }

}
